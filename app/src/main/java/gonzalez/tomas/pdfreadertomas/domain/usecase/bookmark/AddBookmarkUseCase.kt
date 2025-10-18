package gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark

import gonzalez.tomas.pdfreadertomas.domain.model.Bookmark
import gonzalez.tomas.pdfreadertomas.domain.repository.BookmarkRepository
import javax.inject.Inject
import java.util.Date

/**
 * Caso de uso para añadir o actualizar un marcador en un documento
 */
class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(documentId: Long, page: Int, note: String? = null): Result<Long> {
        return try {
            // Verificar si ya existe un marcador para esta página
            val existingBookmark = bookmarkRepository.getBookmarkForPage(documentId, page)

            val bookmarkId = if (existingBookmark != null) {
                // Actualizar el marcador existente (por ejemplo, para cambiar la nota)
                val updatedBookmark = existingBookmark.copy(note = note)
                bookmarkRepository.updateBookmark(updatedBookmark)
                existingBookmark.id
            } else {
                // Crear un nuevo marcador
                val newBookmark = Bookmark(
                    documentId = documentId,
                    page = page,
                    createdAt = Date(),
                    note = note
                )
                bookmarkRepository.insertBookmark(newBookmark)
            }

            Result.success(bookmarkId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
