package gonzalez.tomas.pdfreadertomas.tts.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.repository.DocumentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import gonzalez.tomas.pdfreadertomas.tts.model.TtsState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase para manejar la conexión con el servicio TTS
 */
@Singleton
class TtsServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository
) {
    private var ttsService: TtsService? = null
    private var isBound = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Job para controlar la recolección del estado
    private var stateCollectionJob: Job? = null

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? TtsService.TtsBinder
            if (binder != null) {
                ttsService = binder.getService()
                isBound = true

                // Cancelar cualquier recolección anterior
                stateCollectionJob?.cancel()

                // Iniciar una nueva recolección del estado del servicio
                stateCollectionJob = serviceScope.launch {
                    ttsService?.ttsState?.collectLatest { serviceState ->
                        _ttsState.value = serviceState
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            ttsService = null

            // Cancelar la recolección cuando el servicio se desconecta
            stateCollectionJob?.cancel()
            stateCollectionJob = null
        }
    }

    /**
     * Iniciar el servicio TTS y vincularlo
     */
    fun bindService() {
        if (!isBound) {
            val intent = Intent(context, TtsService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Desvincular del servicio TTS
     */
    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            stateCollectionJob?.cancel()
            stateCollectionJob = null
        }
    }

    /**
     * Iniciar la lectura de un documento
     */
    suspend fun startReading(
        documentId: Long,
        page: Int = 0
    ) {
        ensureServiceStarted()

        // Obtener el documento desde el repositorio
        val document = documentRepository.getDocumentById(documentId)
        document?.let {
            // Cargar miniatura si existe
            val coverBitmap = it.coverPath?.let { path ->
                try {
                    BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    null
                }
            }

            // Iniciar la lectura en el servicio
            ttsService?.startReading(it, page, coverBitmap)
        }
    }

    /**
     * Pausar la lectura
     */
    fun pauseReading() {
        ttsService?.pauseTts()
    }

    /**
     * Reanudar la lectura
     */
    fun resumeReading() {
        ttsService?.resumeTts()
    }

    /**
     * Detener la lectura
     */
    fun stopReading() {
        ttsService?.stopTts()
    }

    /**
     * Saltar al siguiente párrafo
     */
    fun skipToNextParagraph() {
        ttsService?.nextParagraph()
    }

    /**
     * Saltar al párrafo anterior
     */
    fun skipToPreviousParagraph() {
        ttsService?.previousParagraph()
    }

    /**
     * Asegurarse de que el servicio esté iniciado y vinculado
     */
    private fun ensureServiceStarted() {
        if (ttsService == null || !isBound) {
            val intent = Intent(context, TtsService::class.java)
            context.startForegroundService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
}
