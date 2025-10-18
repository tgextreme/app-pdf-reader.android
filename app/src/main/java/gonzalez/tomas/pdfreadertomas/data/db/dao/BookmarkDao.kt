package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con marcadores
 */
@Dao
interface BookmarkDao {
    /**
     * Obtiene todos los marcadores de un documento ordenados por página
     */
    @Query("SELECT * FROM bookmarks WHERE documentId = :documentId ORDER BY page ASC")
    fun getBookmarksForDocumentFlow(documentId: Long): Flow<List<BookmarkEntity>>

    /**
     * Obtiene un marcador específico por su ID
     */
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: Long): BookmarkEntity?

    /**
     * Comprueba si existe un marcador para una página específica de un documento
     */
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE documentId = :documentId AND page = :page LIMIT 1)")
    suspend fun hasBookmarkForPage(documentId: Long, page: Int): Boolean

    /**
     * Inserta un marcador y devuelve su ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    /**
     * Actualiza un marcador existente
     */
    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    /**
     * Elimina un marcador
     */
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    /**
     * Elimina un marcador específico por documento y página
     */
    @Query("DELETE FROM bookmarks WHERE documentId = :documentId AND page = :page")
    suspend fun deleteBookmarkByPage(documentId: Long, page: Int)

    /**
     * Elimina todos los marcadores de un documento
     */
    @Query("DELETE FROM bookmarks WHERE documentId = :documentId")
    suspend fun deleteAllBookmarksForDocument(documentId: Long)

    /**
     * Obtiene el marcador de una página específica de un documento
     */
    @Query("SELECT * FROM bookmarks WHERE documentId = :documentId AND page = :page LIMIT 1")
    suspend fun getBookmarkForPage(documentId: Long, page: Int): BookmarkEntity?
}
