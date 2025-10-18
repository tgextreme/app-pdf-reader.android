package gonzalez.tomas.pdfreadertomas.domain.model

/**
 * Modelo de dominio para el progreso de lectura de un documento PDF
 */
data class ReadingProgress(
    val documentId: Long,
    val lastPage: Int = 0,
    val lastParagraphIndex: Int = 0,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f
)
