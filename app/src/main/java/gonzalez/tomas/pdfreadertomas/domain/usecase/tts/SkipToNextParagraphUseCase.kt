package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import gonzalez.tomas.pdfreadertomas.tts.service.TtsServiceConnection
import javax.inject.Inject

/**
 * Caso de uso para saltar al siguiente párrafo en la lectura TTS
 */
class SkipToNextParagraphUseCase @Inject constructor(
    private val ttsServiceConnection: TtsServiceConnection
) {
    operator fun invoke() {
        ttsServiceConnection.skipToNextParagraph()
    }
}
