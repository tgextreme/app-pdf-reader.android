package gonzalez.tomas.pdfreadertomas.tts.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import gonzalez.tomas.pdfreadertomas.MainActivity
import gonzalez.tomas.pdfreadertomas.R
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.usecase.tts.ExtractTextFromPageUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.tts.GetTtsParamsUseCase
import gonzalez.tomas.pdfreadertomas.tts.model.PdfParagraph
import gonzalez.tomas.pdfreadertomas.tts.model.TtsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Servicio para la funcionalidad de Text-to-Speech
 */
@AndroidEntryPoint
class TtsService : Service() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "TTS_NOTIFICATION_CHANNEL"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "gonzalez.tomas.pdfreadertomas.ACTION_PLAY"
        const val ACTION_PAUSE = "gonzalez.tomas.pdfreadertomas.ACTION_PAUSE"
        const val ACTION_NEXT = "gonzalez.tomas.pdfreadertomas.ACTION_NEXT"
        const val ACTION_PREVIOUS = "gonzalez.tomas.pdfreadertomas.ACTION_PREVIOUS"
        const val ACTION_STOP = "gonzalez.tomas.pdfreadertomas.ACTION_STOP"
    }

    @Inject
    lateinit var getTtsParamsUseCase: GetTtsParamsUseCase

    @Inject
    lateinit var extractTextFromPageUseCase: ExtractTextFromPageUseCase

    private val binder = TtsBinder()
    private var textToSpeech: TextToSpeech? = null
    private var currentDocument: Document? = null
    private var currentPage: Int = 0
    private var currentCoverBitmap: Bitmap? = null
    private val paragraphs = mutableListOf<PdfParagraph>()
    private var currentParagraphIndex = 0
    private var textExtractionJob: Job? = null

    // Media Session
    private lateinit var mediaSession: MediaSession

    // Estado observable
    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    // CoroutineScope personalizado para reemplazar lifecycleScope
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    inner class TtsBinder : Binder() {
        fun getService(): TtsService = this@TtsService
    }

    override fun onCreate() {
        super.onCreate()

        initTextToSpeech()
        initMediaSession()
        createNotificationChannel()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configurar TextToSpeech con los parámetros guardados
                serviceScope.launch {
                    val params = getTtsParamsUseCase()
                    textToSpeech?.apply {
                        // Convertir String a Locale si es necesario
                        language = if (params.locale is String) {
                            val localeParts = (params.locale as String).split("_")
                            when (localeParts.size) {
                                1 -> Locale(localeParts[0])
                                2 -> Locale(localeParts[0], localeParts[1])
                                3 -> Locale(localeParts[0], localeParts[1], localeParts[2])
                                else -> Locale.getDefault()
                            }
                        } else {
                            params.locale as? Locale ?: Locale.getDefault()
                        }
                        setPitch(params.pitch)
                        setSpeechRate(params.speed)

                        // Configurar listener para eventos de TextToSpeech
                        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                // No es necesario hacer nada aquí
                            }

                            override fun onDone(utteranceId: String?) {
                                // Cuando termina de hablar un párrafo, pasar al siguiente
                                serviceScope.launch {
                                    nextParagraph()
                                }
                            }

                            @Deprecated("Deprecado en API level 21")
                            override fun onError(utteranceId: String?) {
                                // Manejar error
                                _ttsState.update { it.copy(error = "Error en la síntesis de voz") }
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                super.onError(utteranceId, errorCode)
                                _ttsState.update {
                                    it.copy(error = "Error en la síntesis de voz. Código: $errorCode")
                                }
                            }
                        })
                    }
                }
            } else {
                _ttsState.update { it.copy(error = "Error al inicializar TextToSpeech") }
            }
        }
    }

    private fun initMediaSession() {
        // Inicializar MediaSession para la integración con el sistema de medios de Android
        // MediaSession.Builder requiere un Player no nulo como segundo parámetro
        // Creamos una instancia de ExoPlayer para satisfacer el requerimiento
        val player = androidx.media3.exoplayer.ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    // Añadir explícitamente override para asegurarnos que Kapt reconozca que estamos extendiendo Service
    override fun onBind(intent: Intent): IBinder {
        // Eliminamos la llamada super.onBind(intent) ya que onBind es abstracto en Service
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> resumeTts()
            ACTION_PAUSE -> pauseTts()
            ACTION_NEXT -> nextParagraph()
            ACTION_PREVIOUS -> previousParagraph()
            ACTION_STOP -> stopTts()
        }
    }

    fun startReading(document: Document, page: Int, coverBitmap: Bitmap?) {
        currentDocument = document
        currentPage = page
        currentCoverBitmap = coverBitmap
        currentParagraphIndex = 0
        paragraphs.clear()

        updateMediaMetadata(document)

        // Extraer texto de la página
        extractTextFromPage(document.uri, page)
    }

    private fun extractTextFromPage(documentUri: String, page: Int) {
        _ttsState.update { it.copy(isLoading = true, currentPage = page) }

        textExtractionJob?.cancel()
        textExtractionJob = serviceScope.launch {
            try {
                val extractedParagraphs = extractTextFromPageUseCase(Uri.parse(documentUri), page)
                paragraphs.clear()
                paragraphs.addAll(extractedParagraphs)

                _ttsState.update {
                    it.copy(
                        isLoading = false,
                        currentPage = page,
                        currentParagraph = if (extractedParagraphs.isNotEmpty()) extractedParagraphs[0] else null,
                        paragraphCount = extractedParagraphs.size
                    )
                }

                if (paragraphs.isNotEmpty()) {
                    speakCurrentParagraph()
                }
            } catch (e: Exception) {
                _ttsState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al extraer texto: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun speakCurrentParagraph() {
        if (paragraphs.isEmpty() || currentParagraphIndex >= paragraphs.size) {
            return
        }

        val paragraph = paragraphs[currentParagraphIndex]
        _ttsState.update {
            it.copy(
                isPlaying = true,
                currentParagraph = paragraph,
                currentParagraphIndex = currentParagraphIndex
            )
        }

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString())

        textToSpeech?.speak(paragraph.text, TextToSpeech.QUEUE_FLUSH, params, paragraph.id)
        updateNotification()
    }

    fun resumeTts() {
        if (textToSpeech?.isSpeaking == false) {
            speakCurrentParagraph()
        }
    }

    fun pauseTts() {
        textToSpeech?.stop()
        _ttsState.update { it.copy(isPlaying = false) }
        updateNotification()
    }

    fun nextParagraph() {
        textToSpeech?.stop()

        if (currentParagraphIndex < paragraphs.size - 1) {
            currentParagraphIndex++
            speakCurrentParagraph()
        } else {
            // Fin de página, cargar siguiente si existe
            val nextPage = currentPage + 1
            currentDocument?.let {
                if (nextPage < it.pageCount) {
                    currentPage = nextPage
                    currentParagraphIndex = 0
                    extractTextFromPage(it.uri, nextPage)
                    updateMediaMetadata(it)
                } else {
                    // Fin del documento
                    _ttsState.update { state ->
                        state.copy(isPlaying = false, isFinished = true)
                    }
                    stopForeground(true)
                }
            }
        }
        updateNotification()
    }

    fun previousParagraph() {
        textToSpeech?.stop()

        if (currentParagraphIndex > 0) {
            currentParagraphIndex--
            speakCurrentParagraph()
        } else {
            // Principio de página, cargar anterior si existe
            val prevPage = currentPage - 1
            if (prevPage >= 0) {
                currentDocument?.let {
                    currentPage = prevPage
                    extractTextFromPage(it.uri, prevPage)
                    // Se ajustará el índice al final de los párrafos cuando se carguen
                    currentParagraphIndex = 0
                    updateMediaMetadata(it)
                }
            }
        }
        updateNotification()
    }

    fun stopTts() {
        textToSpeech?.stop()
        _ttsState.update { it.copy(isPlaying = false) }
        stopForeground(true)
        stopSelf()
    }

    fun setSpeed(speed: Float) {
        textToSpeech?.setSpeechRate(speed)
    }

    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }

    fun setVoice(voice: Voice) {
        textToSpeech?.voice = voice
    }

    fun setLocale(locale: Locale) {
        textToSpeech?.language = locale
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "TTS Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del servicio de lectura TTS"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateMediaMetadata(document: Document) {
        // Actualizar metadatos para la sesión multimedia
        val mediaItem = MediaItem.Builder()
            .setMediaId(document.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(document.title)
                    .setArtist(document.author ?: "Desconocido")
                    .setAlbumTitle("Página ${currentPage + 1}/${document.pageCount}")
                    .build()
            )
            .build()

        // En un caso real, actualizaríamos el player con este mediaItem
    }

    private fun updateNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val document = currentDocument ?: return createEmptyNotification()

        // Intents para los botones de la notificación
        val playIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TtsService::class.java).setAction(ACTION_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(
            this, 2,
            Intent(this, TtsService::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 3,
            Intent(this, TtsService::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 4,
            Intent(this, TtsService::class.java).setAction(ACTION_PREVIOUS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 5,
            Intent(this, TtsService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para abrir la app
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = _ttsState.value.isPlaying

        // Crear notificación con controles multimedia
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(document.title)
            .setContentText("Página ${currentPage + 1}/${document.pageCount}")
            .setSubText(document.author ?: "")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(currentCoverBitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_pdf_placeholder))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .addAction(R.drawable.ic_skip_previous, "Anterior", prevIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pausar" else "Reproducir",
                if (isPlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.ic_skip_next, "Siguiente", nextIntent)
            .addAction(R.drawable.ic_stop, "Detener", stopIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()
    }

    private fun createEmptyNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Lector PDF")
            .setContentText("Servicio TTS activo")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        mediaSession.release()
        serviceScope.cancel() // Cancelar el CoroutineScope al destruir el servicio
    }
}
