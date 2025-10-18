package gonzalez.tomas.pdfreadertomas.domain.usecase.reading

import gonzalez.tomas.pdfreadertomas.domain.model.ReadingProgress
import gonzalez.tomas.pdfreadertomas.domain.repository.DocumentRepository
import gonzalez.tomas.pdfreadertomas.domain.repository.ReadingProgressRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar el progreso de lectura
 */
class UpdateReadingProgressUseCase @Inject constructor(
    private val readingProgressRepository: ReadingProgressRepository,
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(
        documentId: Long,
        page: Int,
        paragraphIndex: Int = 0,
        updateDocumentProgress: Boolean = true
    ): Result<Unit> {
        return try {
            // Actualizar el progreso de lectura detallado
            readingProgressRepository.updateReadingPosition(documentId, page, paragraphIndex)

            // Actualizar también el progreso en el documento si se solicita
            if (updateDocumentProgress) {
                val document = documentRepository.getDocumentById(documentId)
                if (document != null) {
                    val pageCount = document.pageCount
                    if (pageCount > 0) {
                        // Calcular el progreso como porcentaje de páginas leídas
                        val progress = page.toFloat() / pageCount.toFloat()
                        documentRepository.updateReadingProgress(documentId, page, progress)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
