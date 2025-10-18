package gonzalez.tomas.pdfreadertomas.domain.model

import java.util.Date

/**
 * Modelo de dominio para una nota asociada a una página de un documento PDF
 */
data class Note(
    val id: Long = 0,
    val documentId: Long,
    val page: Int,
    val content: String,
    val createdAt: Date = Date()
)
