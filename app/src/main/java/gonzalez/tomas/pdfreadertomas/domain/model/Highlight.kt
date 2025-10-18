package gonzalez.tomas.pdfreadertomas.domain.model

import java.util.Date

/**
 * Modelo de dominio para un resaltado de texto en un documento PDF
 */
data class Highlight(
    val id: Long = 0,
    val documentId: Long,
    val page: Int,
    val rect: String, // JSON serializado de los rectángulos que forman el highlight
    val color: Int, // Color ARGB del resaltado
    val createdAt: Date = Date(),
    val textOpt: String? = null // Texto resaltado (opcional)
)
