package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.ReadingProgressDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.ReadingProgressEntity
import gonzalez.tomas.pdfreadertomas.domain.model.ReadingProgress
import gonzalez.tomas.pdfreadertomas.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingProgressRepositoryImpl @Inject constructor(
    private val readingProgressDao: ReadingProgressDao
) : ReadingProgressRepository {

    override suspend fun getReadingProgressForDocument(documentId: Long): ReadingProgress? {
        val entity = readingProgressDao.getReadingProgressForDocument(documentId) ?: return null
        return entity.toDomain()
    }

    override fun getReadingProgressForDocumentFlow(documentId: Long): Flow<ReadingProgress?> {
        return readingProgressDao.getReadingProgressForDocumentFlow(documentId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertOrUpdateReadingProgress(readingProgress: ReadingProgress) {
        readingProgressDao.insertOrUpdateReadingProgress(readingProgress.toEntity())
    }

    override suspend fun updateReadingPosition(documentId: Long, page: Int, paragraphIndex: Int) {
        readingProgressDao.updateReadingPosition(documentId, page, paragraphIndex)
    }

    override suspend fun updateTtsSettings(documentId: Long, speed: Float, pitch: Float) {
        readingProgressDao.updateTtsSettings(documentId, speed, pitch)
    }

    override suspend fun deleteReadingProgress(documentId: Long) {
        readingProgressDao.deleteReadingProgress(documentId)
    }

    // Extension functions para mapear entre entidades y modelos de dominio
    private fun ReadingProgressEntity.toDomain(): ReadingProgress {
        return ReadingProgress(
            documentId = this.documentId,
            lastPage = this.lastPage,
            lastParagraphIndex = this.lastParagraphIndex,
            ttsSpeed = this.ttsSpeed,
            ttsPitch = this.ttsPitch
        )
    }

    private fun ReadingProgress.toEntity(): ReadingProgressEntity {
        return ReadingProgressEntity(
            documentId = this.documentId,
            lastPage = this.lastPage,
            lastParagraphIndex = this.lastParagraphIndex,
            ttsSpeed = this.ttsSpeed,
            ttsPitch = this.ttsPitch
        )
    }
}
