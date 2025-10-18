package gonzalez.tomas.pdfreadertomas.tts.model

import android.graphics.RectF

/**
 * Representa un párrafo extraído de un PDF con su texto y coordenadas para resaltado
 */
data class PdfParagraph(
    val id: String,
    val text: String,
    val pageNumber: Int,
    val boundingBox: RectF? = null, // Área que ocupa en la página para resaltado
    val charBoundingBoxes: List<RectF> = emptyList() // Para resaltado de palabras individuales
)
