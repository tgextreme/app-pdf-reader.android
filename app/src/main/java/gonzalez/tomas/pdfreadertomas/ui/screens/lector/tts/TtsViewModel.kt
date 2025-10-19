package gonzalez.tomas.pdfreadertomas.ui.screens.lector.tts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.usecase.tts.SaveTtsParamsUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.tts.SleepTimerUseCase
import gonzalez.tomas.pdfreadertomas.tts.engine.TtsEngine
import gonzalez.tomas.pdfreadertomas.tts.model.TtsState
import gonzalez.tomas.pdfreadertomas.tts.service.TtsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TtsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveTtsParamsUseCase: SaveTtsParamsUseCase,
    private val sleepTimerUseCase: SleepTimerUseCase,
    private val ttsEngine: TtsEngine
) : ViewModel() {

    private var ttsService: TtsService? = null
    private var serviceBound = false

    // Estado del TTS observado desde UI
    private val _ttsUiState = MutableStateFlow(TtsState())
    val ttsUiState: StateFlow<TtsState> = _ttsUiState.asStateFlow()

    // Estado del temporizador
    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    // Estado para la selección de voces
    private val _voicesUiState = MutableStateFlow(VoicesUiState())
    val voicesUiState: StateFlow<VoicesUiState> = _voicesUiState.asStateFlow()

    // Job para el temporizador
    private var timerJob: Job? = null

    // Inicializar TTS Engine
    init {
        initTtsEngine()
    }

    private fun initTtsEngine() {
        viewModelScope.launch {
            _voicesUiState.update { it.copy(isLoading = true) }

            ttsEngine.initialize { success ->
                if (success) {
                    viewModelScope.launch {
                        val engineState = ttsEngine.state.value
                        _voicesUiState.update { state ->
                            state.copy(
                                isLoading = false,
                                availableVoices = engineState.availableVoices,
                                currentVoice = engineState.currentVoice,
                                currentLocale = engineState.currentLocale
                            )
                        }
                    }
                } else {
                    _voicesUiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al inicializar el motor TTS"
                        )
                    }
                }
            }
        }
    }

    // Conexión con el servicio TTS
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TtsService.TtsBinder
            ttsService = binder.getService()
            serviceBound = true

            // Comenzar a observar el estado del servicio
            viewModelScope.launch {
                ttsService?.ttsState?.collect { serviceState ->
                    _ttsUiState.update { serviceState }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ttsService = null
            serviceBound = false
            _ttsUiState.update { TtsState(error = "Servicio TTS desconectado") }
        }
    }

    /**
     * Iniciar el servicio TTS y conectarse a él
     */
    fun bindTtsService() {
        if (!serviceBound) {
            val intent = Intent(context, TtsService::class.java)
            // Asegurar que el servicio se inicie en primer plano cuando corresponda
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignorar si startForegroundService falla; aun así intentamos bind
            }

            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Desconectar del servicio TTS
     */
    fun unbindTtsService() {
        if (serviceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                // ignore
            }
            serviceBound = false
        }
    }

    /**
     * Inicia la lectura TTS de un documento desde una página específica
     */
    fun startReading(document: Document, page: Int, coverBitmap: Bitmap?) {
        // Intentamos iniciar y bindear el servicio
        bindTtsService()

        // Lanzar una coroutine para esperar un corto periodo a que el servicio quede ligado
        viewModelScope.launch {
            val maxWaitMs = 2000L
            var waited = 0L
            val interval = 100L
            while (!serviceBound && waited < maxWaitMs) {
                delay(interval)
                waited += interval
            }

            // Si está ligado, llamamos a startReading; si no, intentamos iniciar por intent
            if (serviceBound && ttsService != null) {
                ttsService?.startReading(document, page, coverBitmap)
            } else {
                // Como fallback, iniciar servicio con intent y extras (si necesario)
                val intent = Intent(context, TtsService::class.java).apply {
                    // Podríamos añadir extras si el servicio los consume desde onStartCommand
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    /**
     * Controla la reproducción (play/pause)
     */
    fun togglePlayback() {
        if (_ttsUiState.value.isPlaying) {
            pauseTts()
        } else {
            playTts()
        }
    }

    /**
     * Reanudar la reproducción TTS
     */
    fun playTts() {
        ttsService?.resumeTts()
    }

    /**
     * Pausar la reproducción TTS
     */
    fun pauseTts() {
        ttsService?.pauseTts()
    }

    /**
     * Ir al párrafo siguiente
     */
    fun nextParagraph() {
        ttsService?.nextParagraph()
    }

    /**
     * Ir al párrafo anterior
     */
    fun previousParagraph() {
        ttsService?.previousParagraph()
    }

    /**
     * Detener completamente la reproducción TTS
     */
    fun stopTts() {
        ttsService?.stopTts()
        cancelSleepTimer()
    }

    /**
     * Cambiar la velocidad de reproducción
     */
    fun setSpeed(speed: Float) {
        ttsService?.setSpeed(speed)
        ttsEngine.setSpeed(speed)
        viewModelScope.launch {
            saveTtsParamsUseCase.saveSpeed(speed)
        }
    }

    /**
     * Cambiar el tono de reproducción
     */
    fun setPitch(pitch: Float) {
        ttsService?.setPitch(pitch)
        ttsEngine.setPitch(pitch)
        viewModelScope.launch {
            saveTtsParamsUseCase.savePitch(pitch)
        }
    }

    /**
     * Seleccionar una voz TTS
     */
    fun selectVoice(voice: Voice) {
        val success = ttsEngine.setVoice(voice)
        if (success) {
            ttsService?.setVoice(voice)
            _voicesUiState.update {
                it.copy(currentVoice = voice)
            }

            viewModelScope.launch {
                saveTtsParamsUseCase.saveVoiceName(voice.name)
            }
        }
    }

    /**
     * Probar una voz reproduciendo un texto de ejemplo
     */
    fun testVoice(voice: Voice) {
        ttsEngine.setVoice(voice)
        ttsEngine.speak("Este es un ejemplo de texto para probar esta voz.")
    }

    /**
     * Cambiar el idioma de la voz TTS
     */
    fun setLocale(locale: Locale) {
        val success = ttsEngine.setLanguage(locale)
        if (success) {
            ttsService?.setLocale(locale)
            _voicesUiState.update {
                it.copy(currentLocale = locale)
            }

            viewModelScope.launch {
                saveTtsParamsUseCase.saveLocale(locale.toLanguageTag())
            }
        }
    }

    /**
     * Inicia el temporizador de sueño
     * @param durationMinutes Duración en minutos
     */
    fun startSleepTimer(durationMinutes: Int) {
        // Cancelar temporizador anterior si existe
        cancelSleepTimer()

        _sleepTimerState.update {
            it.copy(
                isActive = true,
                durationMinutes = durationMinutes,
                remainingSeconds = durationMinutes * 60,
                formattedTime = sleepTimerUseCase.formatTime(durationMinutes * 60)
            )
        }

        timerJob = viewModelScope.launch {
            sleepTimerUseCase.startTimer(durationMinutes).collectLatest { remainingSeconds ->
                _sleepTimerState.update {
                    it.copy(
                        remainingSeconds = remainingSeconds,
                        formattedTime = sleepTimerUseCase.formatTime(remainingSeconds)
                    )
                }

                if (remainingSeconds <= 0) {
                    // Detener reproducción cuando el temporizador llega a cero
                    stopTts()
                    _sleepTimerState.update { it.copy(isActive = false) }
                }
            }
        }
    }

    /**
     * Cancela el temporizador de sueño actual
     */
    fun cancelSleepTimer() {
        timerJob?.cancel()
        timerJob = null
        _sleepTimerState.update { SleepTimerState() }
    }

    override fun onCleared() {
        unbindTtsService()
        cancelSleepTimer()
        ttsEngine.shutdown()
        super.onCleared()
    }
}

/**
 * Estado del temporizador de sueño
 */
data class SleepTimerState(
    val isActive: Boolean = false,
    val durationMinutes: Int = 0,
    val remainingSeconds: Int = 0,
    val formattedTime: String = "00:00"
)

/**
 * Estado para la selección de voces
 */
data class VoicesUiState(
    val isLoading: Boolean = false,
    val availableVoices: List<Voice> = emptyList(),
    val currentVoice: Voice? = null,
    val currentLocale: Locale? = null,
    val error: String? = null
)
