package gonzalez.tomas.pdfreadertomas.domain.usecase.reading

import gonzalez.tomas.pdfreadertomas.domain.model.ReadingProgress
import gonzalez.tomas.pdfreadertomas.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso para obtener el progreso de lectura de un documento
 */
class GetReadingProgressUseCase @Inject constructor(
    private val readingProgressRepository: ReadingProgressRepository
) {
    suspend operator fun invoke(documentId: Long): ReadingProgress {
        // Intentar obtener el progreso existente o devolver uno predeterminado
        return readingProgressRepository.getReadingProgressForDocument(documentId)
            ?: ReadingProgress(
                documentId = documentId,
                lastPage = 0,
                lastParagraphIndex = 0,
                ttsSpeed = 1.0f,
                ttsPitch = 1.0f
            )
    }

    /**
     * Obtiene el progreso de lectura como un flujo
     */
    fun observeProgress(documentId: Long): Flow<ReadingProgress> {
        return readingProgressRepository.getReadingProgressForDocumentFlow(documentId)
            ?.map { it ?: ReadingProgress(
                documentId = documentId,
                lastPage = 0,
                lastParagraphIndex = 0,
                ttsSpeed = 1.0f,
                ttsPitch = 1.0f
            ) }
            ?: flowOf(
                ReadingProgress(
                    documentId = documentId,
                    lastPage = 0,
                    lastParagraphIndex = 0,
                    ttsSpeed = 1.0f,
                    ttsPitch = 1.0f
                )
            )
    }
}
