package gonzalez.tomas.pdfreadertomas.tts.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import gonzalez.tomas.pdfreadertomas.PdfReaderApplication
import gonzalez.tomas.pdfreadertomas.di.TtsEntryPoint

/**
 * Receptor para manejar acciones desde la notificación TTS
 */
class TtsActionReceiver : BroadcastReceiver() {

    // Se usará inyección manual en lugar de @Inject con @AndroidEntryPoint
    private lateinit var ttsServiceConnection: TtsServiceConnection
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        // Obtenemos la instancia de TtsServiceConnection desde el EntryPoint de Hilt
        val appContext = context.applicationContext as PdfReaderApplication
        val entryPoint = EntryPoints.get(appContext, TtsEntryPoint::class.java)
        ttsServiceConnection = entryPoint.getTtsServiceConnection()

        // Validación del intent
        val action = intent.action ?: return

        when (action) {
            "playpause" -> handlePlayPause()
            "next" -> handleNext()
            "previous" -> handlePrevious()
            "stop" -> handleStop()
        }
    }

    private fun handlePlayPause() {
        scope.launch {
            if (ttsServiceConnection.ttsState.value.isPlaying) {
                ttsServiceConnection.pauseReading()
            } else {
                ttsServiceConnection.resumeReading()
            }
        }
    }

    private fun handleNext() {
        scope.launch {
            ttsServiceConnection.skipToNextParagraph()
        }
    }

    private fun handlePrevious() {
        scope.launch {
            ttsServiceConnection.skipToPreviousParagraph()
        }
    }

    private fun handleStop() {
        scope.launch {
            ttsServiceConnection.stopReading()
        }
    }
}
