package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.BookmarkDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.BookmarkEntity
import gonzalez.tomas.pdfreadertomas.domain.model.Bookmark
import gonzalez.tomas.pdfreadertomas.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarksForDocumentFlow(documentId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksForDocumentFlow(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBookmarkById(id: Long): Bookmark? {
        val entity = bookmarkDao.getBookmarkById(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun hasBookmarkForPage(documentId: Long, page: Int): Boolean {
        return bookmarkDao.hasBookmarkForPage(documentId, page)
    }

    override suspend fun insertBookmark(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark.toEntity())
    }

    override suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(id: Long) {
        bookmarkDao.getBookmarkById(id)?.let {
            bookmarkDao.deleteBookmark(it)
        }
    }

    override suspend fun deleteBookmarkByPage(documentId: Long, page: Int) {
        bookmarkDao.deleteBookmarkByPage(documentId, page)
    }

    override suspend fun deleteAllBookmarksForDocument(documentId: Long) {
        bookmarkDao.deleteAllBookmarksForDocument(documentId)
    }

    override suspend fun getBookmarkForPage(documentId: Long, page: Int): Bookmark? {
        val entity = bookmarkDao.getBookmarkForPage(documentId, page) ?: return null
        return entity.toDomain()
    }

    // Extension functions para mapear entre entidades y modelos de dominio
    private fun BookmarkEntity.toDomain(): Bookmark {
        return Bookmark(
            id = this.id,
            documentId = this.documentId,
            page = this.page,
            createdAt = Date(this.createdAt),
            note = this.note
        )
    }

    private fun Bookmark.toEntity(): BookmarkEntity {
        return BookmarkEntity(
            id = this.id,
            documentId = this.documentId,
            page = this.page,
            createdAt = this.createdAt.time,
            note = this.note
        )
    }
}
