package gonzalez.tomas.pdfreadertomas.domain.usecase.document

import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener un documento PDF específico por su ID
 */
class GetDocumentByIdUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(id: Long): Document? {
        return documentRepository.getDocumentById(id)
    }
}
