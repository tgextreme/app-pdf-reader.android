package gonzalez.tomas.pdfreadertomas.domain.usecase.document

import android.content.ContentResolver
import android.net.Uri
import android.graphics.Bitmap
import androidx.core.net.toUri
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.repository.DocumentRepository
import gonzalez.tomas.pdfreadertomas.pdf.renderer.PdfRendererWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject

/**
 * Caso de uso para importar un documento PDF desde una URI
 */
class ImportPdfDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val pdfRenderer: PdfRendererWrapper,
    private val contentResolver: ContentResolver
) {
    suspend operator fun invoke(uri: Uri, cachePath: File): Result<Document> = withContext(Dispatchers.IO) {
        try {
            // Verificar si el documento ya existe
            val existingUri = uri.toString()
            if (documentRepository.documentExistsByUri(existingUri)) {
                val document = documentRepository.getDocumentByUri(existingUri)
                if (document != null) {
                    return@withContext Result.success(document)
                }
            }

            // Obtener metadatos básicos
            val fileName = getFileNameFromUri(uri) ?: "PDF sin título"

            // Obtener número de páginas
            // Abrimos el documento para obtener el número de páginas
            pdfRenderer.openDocument(uri)
            val pageCount = pdfRenderer.getPageCount()
            if (pageCount <= 0) {
                return@withContext Result.failure(Exception("No se pudo abrir el PDF o no contiene páginas"))
            }

            // Generar miniatura/portada y guardarla
            // Usamos un ancho estándar para la miniatura
            val thumbnailWidth = 300
            val bitmap = pdfRenderer.generateThumbnail(uri, thumbnailWidth)

            // Guardar el bitmap en el directorio de caché si se generó correctamente
            val thumbnailPath = if (bitmap != null) {
                val thumbnailFile = File(cachePath, "thumbnail_${System.currentTimeMillis()}.jpg")
                thumbnailFile.outputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                }
                thumbnailFile.absolutePath
            } else null

            // Crear el modelo de documento
            val document = Document(
                uri = uri.toString(),
                title = fileName,
                author = null, // Se podría extraer con PDFBox si es necesario
                pageCount = pageCount,
                addedAt = Date(),
                lastOpenedAt = Date(),
                lastPage = 0,
                progressFloat = 0f,
                coverPath = thumbnailPath, // Ahora es un String?
                hasText = true // Por defecto asumimos que tiene texto, luego se verifica
            )

            // Guardar en la base de datos
            val docId = documentRepository.insertDocument(document)

            // Cerramos el documento
            pdfRenderer.closeDocument()

            // Devolver el documento con el ID asignado
            return@withContext Result.success(document.copy(id = docId))

        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val result = when {
            uri.scheme == "content" -> {
                // Consultar el nombre del archivo a través del ContentResolver
                try {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val displayNameIndex = cursor.getColumnIndex("_display_name")
                            if (displayNameIndex != -1) {
                                cursor.getString(displayNameIndex)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> {
                // Para esquemas file: u otros, intentar extraer el último segmento
                uri.lastPathSegment
            }
        }

        // Si no se pudo extraer un nombre, devolver null
        if (result.isNullOrBlank()) return null

        // Si el nombre termina en .pdf, quitamos la extensión
        return if (result.lowercase().endsWith(".pdf")) {
            result.substring(0, result.length - 4)
        } else {
            result
        }
    }
}
