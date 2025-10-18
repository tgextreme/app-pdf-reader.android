package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.HighlightDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.HighlightEntity
import gonzalez.tomas.pdfreadertomas.domain.model.Highlight
import gonzalez.tomas.pdfreadertomas.domain.repository.HighlightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighlightRepositoryImpl @Inject constructor(
    private val highlightDao: HighlightDao
) : HighlightRepository {

    override fun getHighlightsForDocumentFlow(documentId: Long): Flow<List<Highlight>> {
        return highlightDao.getHighlightsForDocumentFlow(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHighlightsForPageFlow(documentId: Long, page: Int): Flow<List<Highlight>> {
        return highlightDao.getHighlightsForPageFlow(documentId, page).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHighlightById(id: Long): Highlight? {
        val entity = highlightDao.getHighlightById(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun insertHighlight(highlight: Highlight): Long {
        return highlightDao.insertHighlight(highlight.toEntity())
    }

    override suspend fun updateHighlight(highlight: Highlight) {
        highlightDao.updateHighlight(highlight.toEntity())
    }

    override suspend fun deleteHighlight(id: Long) {
        highlightDao.getHighlightById(id)?.let {
            highlightDao.deleteHighlight(it)
        }
    }

    override suspend fun deleteHighlightsByPage(documentId: Long, page: Int) {
        highlightDao.deleteHighlightsByPage(documentId, page)
    }

    override suspend fun deleteAllHighlightsForDocument(documentId: Long) {
        highlightDao.deleteAllHighlightsForDocument(documentId)
    }

    override suspend fun searchHighlightsByText(documentId: Long, text: String): List<Highlight> {
        return highlightDao.searchHighlightsByText(documentId, text).map { it.toDomain() }
    }

    // Extension functions para mapear entre entidades y modelos de dominio
    private fun HighlightEntity.toDomain(): Highlight {
        return Highlight(
            id = this.id,
            documentId = this.documentId,
            page = this.page,
            rect = this.rect,
            color = this.color,
            createdAt = Date(this.createdAt),
            textOpt = this.textOpt
        )
    }

    private fun Highlight.toEntity(): HighlightEntity {
        return HighlightEntity(
            id = this.id,
            documentId = this.documentId,
            page = this.page,
            rect = this.rect,
            color = this.color,
            createdAt = this.createdAt.time,
            textOpt = this.textOpt
        )
    }
}
