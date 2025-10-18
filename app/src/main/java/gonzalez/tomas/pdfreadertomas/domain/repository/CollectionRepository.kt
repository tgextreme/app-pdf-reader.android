package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.Collection
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar colecciones de documentos
 */
interface CollectionRepository {
    /**
     * Obtiene todas las colecciones
     */
    fun getAllCollectionsFlow(): Flow<List<Collection>>

    /**
     * Obtiene una colección específica por su ID
     */
    suspend fun getCollectionById(collectionId: Long): Collection?

    /**
     * Inserta una nueva colección
     * @return ID de la colección insertada
     */
    suspend fun insertCollection(collection: Collection): Long

    /**
     * Actualiza una colección existente
     */
    suspend fun updateCollection(collection: Collection)

    /**
     * Elimina una colección
     */
    suspend fun deleteCollection(id: Long)

    /**
     * Obtiene todas las colecciones a las que pertenece un documento
     */
    fun getCollectionsForDocumentFlow(documentId: Long): Flow<List<Collection>>

    /**
     * Obtiene todos los documentos que pertenecen a una colección
     */
    fun getDocumentsInCollectionFlow(collectionId: Long): Flow<List<Document>>

    /**
     * Agrega un documento a una colección
     */
    suspend fun addDocumentToCollection(documentId: Long, collectionId: Long)

    /**
     * Elimina un documento de una colección
     */
    suspend fun removeDocumentFromCollection(documentId: Long, collectionId: Long)

    /**
     * Comprueba si un documento pertenece a una colección
     */
    suspend fun isDocumentInCollection(documentId: Long, collectionId: Long): Boolean

    /**
     * Obtiene el conteo de documentos en una colección
     */
    suspend fun getDocumentCountInCollection(collectionId: Long): Int
}
