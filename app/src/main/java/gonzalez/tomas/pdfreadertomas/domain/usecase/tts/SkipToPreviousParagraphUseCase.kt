package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import gonzalez.tomas.pdfreadertomas.tts.service.TtsServiceConnection
import javax.inject.Inject

/**
 * Caso de uso para saltar al párrafo anterior en la lectura TTS
 */
class SkipToPreviousParagraphUseCase @Inject constructor(
    private val ttsServiceConnection: TtsServiceConnection
) {
    operator fun invoke() {
        ttsServiceConnection.skipToPreviousParagraph()
    }
}
