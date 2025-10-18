package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import gonzalez.tomas.pdfreadertomas.domain.repository.ReadingProgressRepository
import gonzalez.tomas.pdfreadertomas.pdf.extractor.PdfTextExtractor
import gonzalez.tomas.pdfreadertomas.tts.service.TtsServiceConnection
import javax.inject.Inject

/**
 * Caso de uso para iniciar la lectura TTS de un documento
 */
class StartTtsReadingUseCase @Inject constructor(
    private val ttsServiceConnection: TtsServiceConnection,
    private val pdfTextExtractor: PdfTextExtractor,
    private val readingProgressRepository: ReadingProgressRepository
) {
    suspend operator fun invoke(
        documentId: Long,
        documentTitle: String,
        uri: String,
        page: Int? = null,
        paragraphIndex: Int = 0
    ): Result<Unit> {
        return try {
            // Si no se especifica la página, intentar recuperar el progreso guardado
            val actualPage = page ?: readingProgressRepository.getReadingProgressForDocument(documentId)?.lastPage ?: 0
            val actualParagraph = if (page == null) {
                readingProgressRepository.getReadingProgressForDocument(documentId)?.lastParagraphIndex ?: 0
            } else {
                paragraphIndex
            }

            // Iniciar el servicio TTS
            ttsServiceConnection.startReading(
                documentId = documentId,
                page = actualPage
                // El resto de parámetros se obtendrán desde el repositorio en TtsServiceConnection
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
