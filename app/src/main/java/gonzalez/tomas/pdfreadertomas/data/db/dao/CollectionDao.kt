package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.CollectionEntity
import gonzalez.tomas.pdfreadertomas.data.db.entities.DocumentCollectionEntity
import gonzalez.tomas.pdfreadertomas.data.db.entities.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con colecciones de documentos
 */
@Dao
interface CollectionDao {
    /**
     * Obtiene todas las colecciones ordenadas por nombre
     */
    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollectionsFlow(): Flow<List<CollectionEntity>>

    /**
     * Obtiene una colección específica por su ID
     */
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Long): CollectionEntity?

    /**
     * Inserta una colección y devuelve su ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    /**
     * Actualiza una colección existente
     */
    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    /**
     * Elimina una colección
     */
    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    /**
     * Obtiene todas las colecciones a las que pertenece un documento
     */
    @Query("SELECT c.* FROM collections c INNER JOIN document_collections dc ON c.id = dc.collectionId WHERE dc.documentId = :documentId ORDER BY c.name ASC")
    fun getCollectionsForDocumentFlow(documentId: Long): Flow<List<CollectionEntity>>

    /**
     * Obtiene todos los documentos que pertenecen a una colección
     */
    @Query("SELECT d.* FROM documents d INNER JOIN document_collections dc ON d.id = dc.documentId WHERE dc.collectionId = :collectionId ORDER BY d.title ASC")
    fun getDocumentsInCollectionFlow(collectionId: Long): Flow<List<DocumentEntity>>

    /**
     * Agrega un documento a una colección
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addDocumentToCollection(documentCollection: DocumentCollectionEntity)

    /**
     * Elimina un documento de una colección
     */
    @Query("DELETE FROM document_collections WHERE documentId = :documentId AND collectionId = :collectionId")
    suspend fun removeDocumentFromCollection(documentId: Long, collectionId: Long)

    /**
     * Comprueba si un documento pertenece a una colección
     */
    @Query("SELECT EXISTS(SELECT 1 FROM document_collections WHERE documentId = :documentId AND collectionId = :collectionId LIMIT 1)")
    suspend fun isDocumentInCollection(documentId: Long, collectionId: Long): Boolean

    /**
     * Obtiene el conteo de documentos en una colección
     */
    @Query("SELECT COUNT(*) FROM document_collections WHERE collectionId = :collectionId")
    suspend fun getDocumentCountInCollection(collectionId: Long): Int
}
