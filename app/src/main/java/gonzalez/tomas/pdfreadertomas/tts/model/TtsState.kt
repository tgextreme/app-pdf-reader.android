package gonzalez.tomas.pdfreadertomas.tts.model

import gonzalez.tomas.pdfreadertomas.domain.model.Document

/**
 * Estado del servicio TTS que se expone a la UI
 */
data class TtsState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val currentDocument: Document? = null,
    val currentPage: Int = 0,
    val currentParagraph: PdfParagraph? = null,
    val currentParagraphIndex: Int = 0,
    val paragraphCount: Int = 0,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val error: String? = null
)
