package gonzalez.tomas.pdfreadertomas.pdf.extractor

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase para extraer texto de archivos PDF usando PDFBox
 */
@Singleton
class PdfTextExtractor @Inject constructor(
    private val context: Context
) {
    /**
     * Extrae el texto de una página específica de un PDF
     * @param uri URI del documento PDF
     * @param page Número de página (0-indexed)
     * @return Texto extraído de la página o cadena vacía si hay un error
     */
    suspend fun extractText(uri: String, page: Int): String = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val bufferedInputStream = BufferedInputStream(inputStream)
                PDDocument.load(bufferedInputStream).use { document ->
                    val stripper = PDFTextStripper()
                    stripper.startPage = page + 1 // PDFBox usa índices 1-based
                    stripper.endPage = page + 1
                    stripper.sortByPosition = true

                    return@withContext stripper.getText(document).trim()
                }
            } ?: return@withContext ""
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ""
        }
    }

    /**
     * Divide el texto extraído en párrafos
     * @param text Texto a dividir
     * @return Lista de párrafos
     */
    fun splitIntoParagraphs(text: String): List<String> {
        // Algoritmo simple de división por líneas vacías
        val paragraphs = text.split("\n\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Si no hay párrafos definidos, dividimos por líneas
        return if (paragraphs.isEmpty()) {
            text.split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else {
            paragraphs
        }
    }

    /**
     * Extrae el texto de todas las páginas de un PDF
     * @param uri URI del documento PDF
     * @return Mapa con el número de página como clave y el texto como valor
     */
    suspend fun extractTextFromDocument(uri: String): Map<Int, String> = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val bufferedInputStream = BufferedInputStream(inputStream)
                PDDocument.load(bufferedInputStream).use { document ->
                    val pageCount = document.numberOfPages
                    val result = mutableMapOf<Int, String>()

                    for (i in 0 until pageCount) {
                        val stripper = PDFTextStripper()
                        stripper.startPage = i + 1
                        stripper.endPage = i + 1
                        stripper.sortByPosition = true

                        result[i] = stripper.getText(document).trim()
                    }

                    return@withContext result
                }
            } ?: return@withContext emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyMap()
        }
    }

    /**
     * Comprueba si un PDF tiene texto que se puede extraer
     * @param uri URI del documento PDF
     * @return true si el PDF tiene texto extraíble, false en caso contrario
     */
    suspend fun hasExtractableText(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val bufferedInputStream = BufferedInputStream(inputStream)
                PDDocument.load(bufferedInputStream).use { document ->
                    if (document.numberOfPages == 0) return@withContext false

                    val stripper = PDFTextStripper()
                    stripper.startPage = 1
                    stripper.endPage = 1

                    val text = stripper.getText(document).trim()
                    return@withContext text.isNotEmpty()
                }
            } ?: return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Extrae los metadatos básicos del PDF (título, autor)
     * @param uri URI del documento PDF
     * @return Pair con título y autor, o null si hay un error
     */
    suspend fun extractMetadata(uri: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val bufferedInputStream = BufferedInputStream(inputStream)
                PDDocument.load(bufferedInputStream).use { document ->
                    val info = document.documentInformation
                    val title = info?.title
                    val author = info?.author

                    return@withContext Pair(title, author)
                }
            } ?: return@withContext Pair(null, null)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair(null, null)
        }
    }
}
