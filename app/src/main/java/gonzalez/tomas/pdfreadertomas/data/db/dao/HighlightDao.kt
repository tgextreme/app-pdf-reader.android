package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.HighlightEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con resaltados de texto
 */
@Dao
interface HighlightDao {
    /**
     * Obtiene todos los resaltados de un documento ordenados por página
     */
    @Query("SELECT * FROM highlights WHERE documentId = :documentId ORDER BY page ASC")
    fun getHighlightsForDocumentFlow(documentId: Long): Flow<List<HighlightEntity>>

    /**
     * Obtiene los resaltados para una página específica de un documento
     */
    @Query("SELECT * FROM highlights WHERE documentId = :documentId AND page = :page")
    fun getHighlightsForPageFlow(documentId: Long, page: Int): Flow<List<HighlightEntity>>

    /**
     * Obtiene un resaltado específico por su ID
     */
    @Query("SELECT * FROM highlights WHERE id = :id")
    suspend fun getHighlightById(id: Long): HighlightEntity?

    /**
     * Inserta un resaltado y devuelve su ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity): Long

    /**
     * Actualiza un resaltado existente
     */
    @Update
    suspend fun updateHighlight(highlight: HighlightEntity)

    /**
     * Elimina un resaltado
     */
    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)

    /**
     * Elimina todos los resaltados de una página específica
     */
    @Query("DELETE FROM highlights WHERE documentId = :documentId AND page = :page")
    suspend fun deleteHighlightsByPage(documentId: Long, page: Int)

    /**
     * Elimina todos los resaltados de un documento
     */
    @Query("DELETE FROM highlights WHERE documentId = :documentId")
    suspend fun deleteAllHighlightsForDocument(documentId: Long)

    /**
     * Busca resaltados por texto (útil para búsqueda en documentos)
     */
    @Query("SELECT * FROM highlights WHERE documentId = :documentId AND textOpt LIKE '%' || :text || '%'")
    suspend fun searchHighlightsByText(documentId: Long, text: String): List<HighlightEntity>
}
