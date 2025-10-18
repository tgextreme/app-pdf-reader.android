package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.Document
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar documentos PDF
 */
interface DocumentRepository {
    /**
     * Obtiene un flujo de todos los documentos
     */
    fun getAllDocumentsFlow(): Flow<List<Document>>

    /**
     * Obtiene un documento por su ID
     */
    suspend fun getDocumentById(id: Long): Document?

    /**
     * Inserta un nuevo documento
     * @return ID del documento insertado
     */
    suspend fun insertDocument(document: Document): Long

    /**
     * Actualiza un documento existente
     */
    suspend fun updateDocument(document: Document)

    /**
     * Elimina un documento
     */
    suspend fun deleteDocument(id: Long)

    /**
     * Actualiza el progreso de lectura de un documento
     */
    suspend fun updateReadingProgress(id: Long, page: Int, progress: Float)

    /**
     * Comprueba si existe un documento con la URI especificada
     */
    suspend fun documentExistsByUri(uri: String): Boolean

    /**
     * Obtiene un documento por su URI
     */
    suspend fun getDocumentByUri(uri: String): Document?

    /**
     * Busca documentos por título o autor
     */
    fun searchDocumentsFlow(query: String): Flow<List<Document>>
}
