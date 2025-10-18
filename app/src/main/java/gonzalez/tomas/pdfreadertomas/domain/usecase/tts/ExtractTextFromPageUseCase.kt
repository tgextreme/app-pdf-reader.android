package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.tts.model.PdfParagraph
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso para extraer texto de una página de PDF y convertirlo en párrafos
 */
class ExtractTextFromPageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Extrae el texto de una página específica de un PDF y lo divide en párrafos
     * @param uri URI del documento PDF
     * @param pageNumber Número de página (base 0)
     * @return Lista de párrafos extraídos con sus coordenadas para resaltado
     */
    suspend operator fun invoke(uri: Uri, pageNumber: Int): List<PdfParagraph> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Cargar documento con PDFBox
                val document = PDDocument.load(inputStream)

                // Asegurar que la página existe
                if (pageNumber >= document.numberOfPages) {
                    document.close()
                    return emptyList()
                }

                // Configurar el stripper para extraer texto con información de posición
                val stripper = object : PDFTextStripper() {
                    val textPositions = mutableListOf<TextPosition>()
                    val paragraphTexts = mutableListOf<String>()
                    val paragraphBounds = mutableListOf<RectF>()

                    init {
                        sortByPosition = true
                        startPage = pageNumber + 1 // PDFBox usa base 1 para páginas
                        endPage = pageNumber + 1
                    }

                    override fun writeString(text: String, textPositions: List<TextPosition>) {
                        // Acumular posiciones para calcular bounding boxes
                        this.textPositions.addAll(textPositions)

                        // Detectar párrafos por saltos de línea o espacios grandes
                        if (text.trim().isNotEmpty()) {
                            // Si hay una línea en blanco o un salto explícito, considerarlo un nuevo párrafo
                            if (text.contains("\n\n") || text.endsWith("\n")) {
                                val paragraphs = text.split("\n\n")
                                paragraphTexts.addAll(paragraphs.filter { it.trim().isNotEmpty() })

                                // Calcular bounding box aproximado para cada párrafo
                                if (textPositions.isNotEmpty()) {
                                    val minX = textPositions.minOf { it.xDirAdj }
                                    val minY = textPositions.minOf { it.yDirAdj }
                                    val maxX = textPositions.maxOf { it.xDirAdj + it.width }
                                    val maxY = textPositions.maxOf { it.yDirAdj + it.height }

                                    paragraphBounds.add(RectF(minX, minY, maxX, maxY))
                                }
                            } else {
                                paragraphTexts.add(text.trim())

                                // Calcular bounding box para el texto
                                if (textPositions.isNotEmpty()) {
                                    val minX = textPositions.minOf { it.xDirAdj }
                                    val minY = textPositions.minOf { it.yDirAdj }
                                    val maxX = textPositions.maxOf { it.xDirAdj + it.width }
                                    val maxY = textPositions.maxOf { it.yDirAdj + it.height }

                                    paragraphBounds.add(RectF(minX, minY, maxX, maxY))
                                }
                            }
                        }
                    }

                    // Obtener los párrafos procesados
                    fun getParagraphs(): List<PdfParagraph> {
                        val results = mutableListOf<PdfParagraph>()

                        // Combinar párrafos muy cortos si es necesario
                        var currentText = StringBuilder()
                        var currentBound: RectF? = null

                        for (i in paragraphTexts.indices) {
                            val text = paragraphTexts[i]
                            val bounds = if (i < paragraphBounds.size) paragraphBounds[i] else null

                            // Si el párrafo es muy corto y no termina con punto, combinarlo
                            if (text.length < 40 && !text.endsWith(".") && !text.endsWith("?") && !text.endsWith("!")) {
                                currentText.append(text).append(" ")
                                if (currentBound == null) {
                                    currentBound = bounds
                                } else if (bounds != null) {
                                    currentBound = RectF(
                                        minOf(currentBound.left, bounds.left),
                                        minOf(currentBound.top, bounds.top),
                                        maxOf(currentBound.right, bounds.right),
                                        maxOf(currentBound.bottom, bounds.bottom)
                                    )
                                }
                            } else {
                                // Finalizar párrafo actual si existe
                                if (currentText.isNotEmpty()) {
                                    currentText.append(text)
                                    results.add(
                                        PdfParagraph(
                                            id = UUID.randomUUID().toString(),
                                            text = currentText.toString(),
                                            pageNumber = pageNumber,
                                            boundingBox = currentBound
                                        )
                                    )
                                    currentText = StringBuilder()
                                    currentBound = null
                                } else {
                                    // Añadir párrafo normal
                                    results.add(
                                        PdfParagraph(
                                            id = UUID.randomUUID().toString(),
                                            text = text,
                                            pageNumber = pageNumber,
                                            boundingBox = bounds
                                        )
                                    )
                                }
                            }
                        }

                        // No olvidar el último párrafo si quedó pendiente
                        if (currentText.isNotEmpty()) {
                            results.add(
                                PdfParagraph(
                                    id = UUID.randomUUID().toString(),
                                    text = currentText.toString(),
                                    pageNumber = pageNumber,
                                    boundingBox = currentBound
                                )
                            )
                        }

                        return results
                    }
                }

                // Extraer texto
                stripper.getText(document)
                val paragraphs = stripper.getParagraphs()

                // Liberar recursos
                document.close()

                paragraphs
            } ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}
