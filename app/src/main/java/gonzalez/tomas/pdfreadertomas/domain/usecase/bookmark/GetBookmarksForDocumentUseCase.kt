package gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark

import gonzalez.tomas.pdfreadertomas.domain.model.Bookmark
import gonzalez.tomas.pdfreadertomas.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los marcadores de un documento
 */
class GetBookmarksForDocumentUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(documentId: Long): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarksForDocumentFlow(documentId)
    }
}
