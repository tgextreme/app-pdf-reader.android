package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar notas
 */
interface NoteRepository {
    /**
     * Obtiene todas las notas para un documento
     */
    fun getNotesForDocumentFlow(documentId: Long): Flow<List<Note>>

    /**
     * Obtiene las notas para una página específica de un documento
     */
    fun getNotesForPageFlow(documentId: Long, page: Int): Flow<List<Note>>

    /**
     * Obtiene una nota específica por su ID
     */
    suspend fun getNoteById(id: Long): Note?

    /**
     * Inserta una nueva nota
     * @return ID de la nota insertada
     */
    suspend fun insertNote(note: Note): Long

    /**
     * Actualiza una nota existente
     */
    suspend fun updateNote(note: Note)

    /**
     * Elimina una nota
     */
    suspend fun deleteNote(id: Long)

    /**
     * Elimina todas las notas de una página específica
     */
    suspend fun deleteNotesByPage(documentId: Long, page: Int)

    /**
     * Elimina todas las notas de un documento
     */
    suspend fun deleteAllNotesForDocument(documentId: Long)

    /**
     * Busca notas por contenido
     */
    suspend fun searchNotesByContent(documentId: Long, text: String): List<Note>
}
