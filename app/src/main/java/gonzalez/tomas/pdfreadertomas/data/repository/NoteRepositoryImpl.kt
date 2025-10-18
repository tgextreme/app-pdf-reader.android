package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.NoteDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.NoteEntity
import gonzalez.tomas.pdfreadertomas.domain.model.Note
import gonzalez.tomas.pdfreadertomas.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotesForDocumentFlow(documentId: Long): Flow<List<Note>> {
        return noteDao.getNotesForDocumentFlow(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNotesForPageFlow(documentId: Long, page: Int): Flow<List<Note>> {
        return noteDao.getNotesForPageFlow(documentId, page).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        val entity = noteDao.getNoteById(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.getNoteById(id)?.let {
            noteDao.deleteNote(it)
        }
    }

    override suspend fun deleteNotesByPage(documentId: Long, page: Int) {
        noteDao.deleteNotesByPage(documentId, page)
    }

    override suspend fun deleteAllNotesForDocument(documentId: Long) {
        noteDao.deleteAllNotesForDocument(documentId)
    }

    override suspend fun searchNotesByContent(documentId: Long, text: String): List<Note> {
        return noteDao.searchNotesByContent(documentId, text).map { it.toDomain() }
    }

    // Extension functions para mapear entre entidades y modelos de dominio
    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = this.id,
            documentId = this.documentId,
            page = this.page,
            content = this.content,
            createdAt = Date(this.createdAt)
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = this.id,
            documentId = this.documentId,
            page = this.page,
            content = this.content,
            createdAt = this.createdAt.time
        )
    }
}
