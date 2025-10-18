package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.DocumentDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.DocumentEntity
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getAllDocumentsFlow(): Flow<List<Document>> {
        return documentDao.getAllDocumentsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDocumentById(id: Long): Document? {
        val entity = documentDao.getDocumentById(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun insertDocument(document: Document): Long {
        return documentDao.insertDocument(document.toEntity())
    }

    override suspend fun updateDocument(document: Document) {
        documentDao.updateDocument(document.toEntity())
    }

    override suspend fun deleteDocument(id: Long) {
        documentDao.getDocumentById(id)?.let {
            documentDao.deleteDocument(it)
        }
    }

    override suspend fun updateReadingProgress(id: Long, page: Int, progress: Float) {
        documentDao.updateProgress(id, page, progress)
    }

    override suspend fun documentExistsByUri(uri: String): Boolean {
        return documentDao.documentExistsByUri(uri)
    }

    override suspend fun getDocumentByUri(uri: String): Document? {
        val entity = documentDao.getDocumentByUri(uri) ?: return null
        return entity.toDomain()
    }

    override fun searchDocumentsFlow(query: String): Flow<List<Document>> {
        return documentDao.searchDocumentsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Extension functions para mappear entre entidades y modelos de dominio
    private fun DocumentEntity.toDomain(): Document {
        return Document(
            id = this.id,
            uri = this.uri,
            title = this.title,
            author = this.author,
            pageCount = this.pageCount,
            addedAt = Date(this.addedAt),
            lastOpenedAt = this.lastOpenedAt?.let { Date(it) },
            lastPage = this.lastPage,
            progressFloat = this.progressFloat,
            coverPath = this.coverPath,
            hasText = this.hasText,
            languageHint = this.languageHint
        )
    }

    private fun Document.toEntity(): DocumentEntity {
        return DocumentEntity(
            id = this.id,
            uri = this.uri,
            title = this.title,
            author = this.author,
            pageCount = this.pageCount,
            addedAt = this.addedAt.time,
            lastOpenedAt = this.lastOpenedAt?.time,
            lastPage = this.lastPage,
            progressFloat = this.progressFloat,
            coverPath = this.coverPath,
            hasText = this.hasText,
            languageHint = this.languageHint
        )
    }
}
