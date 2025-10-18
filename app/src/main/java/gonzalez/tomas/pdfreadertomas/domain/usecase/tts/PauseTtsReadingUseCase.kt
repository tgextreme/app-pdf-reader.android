package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import gonzalez.tomas.pdfreadertomas.tts.service.TtsServiceConnection
import javax.inject.Inject

/**
 * Caso de uso para pausar la lectura TTS
 */
class PauseTtsReadingUseCase @Inject constructor(
    private val ttsServiceConnection: TtsServiceConnection
) {
    operator fun invoke() {
        ttsServiceConnection.pauseReading()
    }
}
