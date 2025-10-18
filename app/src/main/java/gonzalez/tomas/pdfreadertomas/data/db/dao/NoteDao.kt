package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con notas
 */
@Dao
interface NoteDao {
    /**
     * Obtiene todas las notas de un documento ordenadas por página
     */
    @Query("SELECT * FROM notes WHERE documentId = :documentId ORDER BY page ASC")
    fun getNotesForDocumentFlow(documentId: Long): Flow<List<NoteEntity>>

    /**
     * Obtiene las notas para una página específica de un documento
     */
    @Query("SELECT * FROM notes WHERE documentId = :documentId AND page = :page")
    fun getNotesForPageFlow(documentId: Long, page: Int): Flow<List<NoteEntity>>

    /**
     * Obtiene una nota específica por su ID
     */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    /**
     * Inserta una nota y devuelve su ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    /**
     * Actualiza una nota existente
     */
    @Update
    suspend fun updateNote(note: NoteEntity)

    /**
     * Elimina una nota
     */
    @Delete
    suspend fun deleteNote(note: NoteEntity)

    /**
     * Elimina todas las notas de una página específica
     */
    @Query("DELETE FROM notes WHERE documentId = :documentId AND page = :page")
    suspend fun deleteNotesByPage(documentId: Long, page: Int)

    /**
     * Elimina todas las notas de un documento
     */
    @Query("DELETE FROM notes WHERE documentId = :documentId")
    suspend fun deleteAllNotesForDocument(documentId: Long)

    /**
     * Busca notas por contenido (útil para búsqueda)
     */
    @Query("SELECT * FROM notes WHERE documentId = :documentId AND content LIKE '%' || :text || '%'")
    suspend fun searchNotesByContent(documentId: Long, text: String): List<NoteEntity>
}
