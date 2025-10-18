package gonzalez.tomas.pdfreadertomas.domain.model

import java.util.Date

/**
 * Modelo de dominio para un marcador en un documento PDF
 */
data class Bookmark(
    val id: Long = 0,
    val documentId: Long,
    val page: Int,
    val createdAt: Date = Date(),
    val note: String? = null
)
