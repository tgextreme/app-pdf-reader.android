package gonzalez.tomas.pdfreadertomas.pdf.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.tts.model.PdfParagraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * Clase para renderizar páginas de PDF usando PdfRenderer de Android
 * y dibujar resaltados sobre ellas.
 */
class PdfRendererWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var currentPage: PdfRenderer.Page? = null
    private var currentUri: Uri? = null

    /**
     * Abre un documento PDF para su renderizado
     */
    suspend fun openDocument(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Cerrar documento anterior si existe
            closeDocument()

            // Abrir nuevo documento
            fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            if (fileDescriptor != null) {
                pdfRenderer = PdfRenderer(fileDescriptor!!)
                currentUri = uri
                true
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene el número total de páginas del PDF
     */
    fun getPageCount(): Int = pdfRenderer?.pageCount ?: 0

    /**
     * Cierra el documento y libera recursos
     */
    fun closeDocument() {
        currentPage?.close()
        currentPage = null
        pdfRenderer?.close()
        pdfRenderer = null
        fileDescriptor?.close()
        fileDescriptor = null
        currentUri = null
    }

    /**
     * Renderiza una página específica del PDF
     * @param pageIndex Índice de la página (base 0)
     * @param width Ancho deseado para la página renderizada
     * @param height Altura deseada para la página renderizada (si es -1, se calcula manteniendo la proporción)
     * @param density Densidad de pantalla para cálculos de escalado
     * @param highlightParagraph Párrafo a resaltar (opcional)
     * @param highlightColor Color para el resaltado
     * @return Bitmap con la página renderizada y el resaltado aplicado
     */
    suspend fun renderPage(
        pageIndex: Int,
        width: Int,
        height: Int = -1,
        density: Density? = null,
        highlightParagraph: PdfParagraph? = null,
        highlightColor: Int = Color.argb(50, 255, 235, 59) // Amarillo semitransparente
    ): Bitmap? = withContext(Dispatchers.IO) {
        pdfRenderer?.let { renderer ->
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                return@withContext null
            }

            try {
                // Cerrar página anterior si estaba abierta
                currentPage?.close()

                // Abrir nueva página
                currentPage = renderer.openPage(pageIndex)
                val page = currentPage ?: return@withContext null

                // Calcular dimensiones manteniendo proporción si se necesita
                val pageWidth = page.width
                val pageHeight = page.height
                val aspectRatio = pageHeight.toFloat() / pageWidth.toFloat()

                val bitmapWidth = width
                val bitmapHeight = if (height == -1) {
                    (width * aspectRatio).toInt()
                } else {
                    height
                }

                // Crear bitmap con el tamaño calculado
                val bitmap = Bitmap.createBitmap(
                    bitmapWidth,
                    bitmapHeight,
                    Bitmap.Config.ARGB_8888
                )

                // Renderizar la página en el bitmap
                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                // Si hay un párrafo para resaltar, dibujarlo sobre el bitmap
                highlightParagraph?.let { paragraph ->
                    if (paragraph.pageNumber == pageIndex && paragraph.boundingBox != null) {
                        val canvas = Canvas(bitmap)
                        val paint = Paint().apply {
                            color = highlightColor
                            style = Paint.Style.FILL
                        }

                        // Convertir las coordenadas del PDF a las del bitmap
                        val boundingBox = paragraph.boundingBox
                        val scaleX = bitmapWidth.toFloat() / pageWidth
                        val scaleY = bitmapHeight.toFloat() / pageHeight

                        val scaledRect = RectF(
                            boundingBox.left * scaleX,
                            boundingBox.top * scaleY,
                            boundingBox.right * scaleX,
                            boundingBox.bottom * scaleY
                        )

                        // Dibujar el resaltado
                        canvas.drawRect(scaledRect, paint)
                    }
                }

                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Genera una miniatura/portada del documento a partir de la primera página
     */
    suspend fun generateThumbnail(
        uri: Uri,
        width: Int,
        height: Int = -1
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Si el documento no está abierto o es diferente, abrirlo
            if (currentUri != uri || pdfRenderer == null) {
                val opened = openDocument(uri)
                if (!opened) return@withContext null
            }

            // Renderizar la primera página como miniatura
            renderPage(0, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte un tamaño de página en puntos PDF a píxeles según la densidad
     */
    fun pdfPointsToPixels(pdfSize: IntSize, density: Density): IntSize {
        val scale = density.density * (72f / 160f) // 72 DPI (puntos PDF) a DPI de Android
        return IntSize(
            (pdfSize.width * scale).toInt(),
            (pdfSize.height * scale).toInt()
        )
    }
}
