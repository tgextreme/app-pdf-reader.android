package gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark

import gonzalez.tomas.pdfreadertomas.domain.repository.BookmarkRepository
import javax.inject.Inject

/**
 * Caso de uso para eliminar un marcador
 */
class DeleteBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            bookmarkRepository.deleteBookmark(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un marcador específico por documento y página
     */
    suspend fun byPage(documentId: Long, page: Int): Result<Unit> {
        return try {
            bookmarkRepository.deleteBookmarkByPage(documentId, page)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
