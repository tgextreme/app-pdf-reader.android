package gonzalez.tomas.pdfreadertomas.tts.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receptor para manejar acciones desde la notificación TTS
 */
@AndroidEntryPoint
class TtsActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var ttsServiceConnection: TtsServiceConnection

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "playpause" -> {
                scope.launch {
                    if (ttsServiceConnection.ttsState.value.isPlaying) {
                        ttsServiceConnection.pauseReading()
                    } else {
                        ttsServiceConnection.resumeReading()
                    }
                }
            }
            "next" -> {
                scope.launch {
                    ttsServiceConnection.skipToNextParagraph()
                }
            }
            "previous" -> {
                scope.launch {
                    ttsServiceConnection.skipToPreviousParagraph()
                }
            }
            "stop" -> {
                scope.launch {
                    ttsServiceConnection.stopReading()
                }
            }
        }
    }
}
