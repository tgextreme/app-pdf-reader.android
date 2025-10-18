package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import gonzalez.tomas.pdfreadertomas.tts.service.TtsServiceConnection
import javax.inject.Inject

/**
 * Caso de uso para detener la lectura TTS
 */
class StopTtsReadingUseCase @Inject constructor(
    private val ttsServiceConnection: TtsServiceConnection
) {
    operator fun invoke() {
        ttsServiceConnection.stopReading()
    }
}
