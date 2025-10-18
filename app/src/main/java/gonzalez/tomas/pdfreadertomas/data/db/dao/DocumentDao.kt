package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con documentos PDF
 */
@Dao
interface DocumentDao {
    /**
     * Obtiene todos los documentos ordenados por fecha de apertura reciente
     */
    @Query("SELECT * FROM documents ORDER BY CASE WHEN lastOpenedAt IS NULL THEN 0 ELSE 1 END DESC, lastOpenedAt DESC")
    fun getAllDocumentsFlow(): Flow<List<DocumentEntity>>

    /**
     * Obtiene documentos para una interfaz paginada
     */
    @Query("SELECT * FROM documents ORDER BY CASE WHEN lastOpenedAt IS NULL THEN 0 ELSE 1 END DESC, lastOpenedAt DESC")
    fun getDocumentsPaged(): PagingSource<Int, DocumentEntity>

    /**
     * Obtiene documentos ordenados por título
     */
    @Query("SELECT * FROM documents ORDER BY title ASC")
    fun getDocumentsByTitleFlow(): Flow<List<DocumentEntity>>

    /**
     * Busca documentos por título o autor
     */
    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchDocumentsFlow(query: String): Flow<List<DocumentEntity>>

    /**
     * Obtiene un documento por su ID
     */
    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: Long): DocumentEntity?

    /**
     * Inserta un documento y devuelve su ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    /**
     * Actualiza un documento existente
     */
    @Update
    suspend fun updateDocument(document: DocumentEntity)

    /**
     * Elimina un documento
     */
    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    /**
     * Actualiza el progreso de lectura de un documento
     */
    @Query("UPDATE documents SET lastPage = :page, lastOpenedAt = :timestamp, progressFloat = :progress WHERE id = :documentId")
    suspend fun updateProgress(documentId: Long, page: Int, progress: Float, timestamp: Long = System.currentTimeMillis())

    /**
     * Comprueba si existe un documento con la URI especificada
     */
    @Query("SELECT EXISTS(SELECT 1 FROM documents WHERE uri = :uri LIMIT 1)")
    suspend fun documentExistsByUri(uri: String): Boolean

    /**
     * Obtiene el documento con la URI especificada
     */
    @Query("SELECT * FROM documents WHERE uri = :uri LIMIT 1")
    suspend fun getDocumentByUri(uri: String): DocumentEntity?
}
