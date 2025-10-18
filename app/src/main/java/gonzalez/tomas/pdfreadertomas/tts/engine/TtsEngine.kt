package gonzalez.tomas.pdfreadertomas.tts.engine

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

/**
 * Wrapper para el motor TextToSpeech de Android
 * que facilita su uso y proporciona un estado observable
 */
class TtsEngine @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "TtsEngine"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    // Estado observable
    private val _state = MutableStateFlow(TtsEngineState())
    val state: StateFlow<TtsEngineState> = _state.asStateFlow()

    // Variables para mantener el seguimiento de los valores actuales
    private var currentSpeechRate = 1.0f
    private var currentPitchValue = 1.0f

    // Inicializar el motor TTS
    fun initialize(onInitialized: ((Boolean) -> Unit)? = null) {
        if (tts != null) {
            shutdown()
        }

        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS

            if (isInitialized) {
                // Actualizar estado con voces disponibles
                val defaultLocale = Locale.getDefault()
                val availableVoices = tts?.voices?.filter { it.locale.language == defaultLocale.language }

                _state.update {
                    it.copy(
                        isInitialized = true,
                        currentVoice = tts?.voice,
                        currentLocale = tts?.language,
                        availableVoices = availableVoices ?: emptyList(),
                        currentSpeed = currentSpeechRate,
                        currentPitch = currentPitchValue
                    )
                }

                // Configurar listener de progreso
                setupProgressListener()
                Log.d(TAG, "TTS inicializado correctamente")
            } else {
                _state.update {
                    it.copy(
                        isInitialized = false,
                        error = "Error al inicializar TTS: código $status"
                    )
                }
                Log.e(TAG, "Error al inicializar TTS: código $status")
            }

            onInitialized?.invoke(isInitialized)
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                _state.update {
                    it.copy(
                        isSpeaking = true,
                        currentUtteranceId = utteranceId
                    )
                }
            }

            override fun onDone(utteranceId: String) {
                _state.update {
                    it.copy(
                        isSpeaking = false,
                        currentUtteranceId = null
                    )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) {
                _state.update {
                    it.copy(
                        isSpeaking = false,
                        currentUtteranceId = null,
                        error = "Error en la reproducción"
                    )
                }
            }
        })
    }

    // Hablar un texto
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH, utteranceId: String = "tts_${System.currentTimeMillis()}"): Boolean {
        if (!isInitialized || tts == null) {
            Log.e(TAG, "Intento de usar TTS sin inicializar")
            return false
        }

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        val result = tts!!.speak(text, queueMode, params, utteranceId)
        return result == TextToSpeech.SUCCESS
    }

    // Detener reproducción
    fun stop() {
        tts?.stop()
        _state.update {
            it.copy(
                isSpeaking = false,
                currentUtteranceId = null
            )
        }
    }

    // Cambiar la velocidad de reproducción
    fun setSpeed(speed: Float) {
        tts?.setSpeechRate(speed)
        currentSpeechRate = speed
        _state.update {
            it.copy(currentSpeed = speed)
        }
    }

    // Cambiar el tono de reproducción
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
        currentPitchValue = pitch
        _state.update {
            it.copy(currentPitch = pitch)
        }
    }

    // Cambiar el idioma
    fun setLanguage(locale: Locale): Boolean {
        val result = tts?.setLanguage(locale) ?: TextToSpeech.ERROR

        if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            _state.update {
                it.copy(currentLocale = locale)
            }
            return true
        }

        return false
    }

    // Cambiar la voz
    fun setVoice(voice: Voice): Boolean {
        val result = tts?.setVoice(voice) ?: -1

        if (result == TextToSpeech.SUCCESS) {
            _state.update {
                it.copy(currentVoice = voice)
            }
            return true
        }

        return false
    }

    // Obtener las voces disponibles para un idioma
    fun getVoicesForLanguage(locale: Locale): List<Voice> {
        return tts?.voices?.filter {
            it.locale.language == locale.language
        } ?: emptyList()
    }

    // Liberar recursos
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _state.update { TtsEngineState() }
    }
}

/**
 * Estado del motor TTS
 */
data class TtsEngineState(
    val isInitialized: Boolean = false,
    val isSpeaking: Boolean = false,
    val currentUtteranceId: String? = null,
    val currentVoice: Voice? = null,
    val currentLocale: Locale? = null,
    val currentSpeed: Float = 1.0f,
    val currentPitch: Float = 1.0f,
    val availableVoices: List<Voice> = emptyList(),
    val error: String? = null
)
