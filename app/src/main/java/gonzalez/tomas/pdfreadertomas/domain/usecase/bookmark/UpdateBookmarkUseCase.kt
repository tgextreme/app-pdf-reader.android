package gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark

import gonzalez.tomas.pdfreadertomas.domain.repository.BookmarkRepository
import javax.inject.Inject

class UpdateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(
        bookmarkId: Long,
        note: String?
    ): Result<Unit> {
        return try {
            // Primero obtener el marcador existente
            val existingBookmark = bookmarkRepository.getBookmarkById(bookmarkId)
            if (existingBookmark != null) {
                // Actualizar solo la nota
                val updatedBookmark = existingBookmark.copy(note = note)
                bookmarkRepository.updateBookmark(updatedBookmark)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Marcador no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
