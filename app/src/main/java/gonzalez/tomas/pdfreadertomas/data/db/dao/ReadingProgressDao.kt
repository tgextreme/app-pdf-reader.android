package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con progreso de lectura
 */
@Dao
interface ReadingProgressDao {
    /**
     * Obtiene el progreso de lectura para un documento específico
     */
    @Query("SELECT * FROM reading_progress WHERE documentId = :documentId")
    suspend fun getReadingProgressForDocument(documentId: Long): ReadingProgressEntity?

    /**
     * Observa el progreso de lectura para un documento mediante Flow
     */
    @Query("SELECT * FROM reading_progress WHERE documentId = :documentId")
    fun getReadingProgressForDocumentFlow(documentId: Long): Flow<ReadingProgressEntity?>

    /**
     * Inserta o actualiza el progreso de lectura para un documento
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReadingProgress(readingProgress: ReadingProgressEntity)

    /**
     * Actualiza el progreso de lectura
     */
    @Update
    suspend fun updateReadingProgress(readingProgress: ReadingProgressEntity)

    /**
     * Actualiza la posición de lectura
     */
    @Query("UPDATE reading_progress SET lastPage = :page, lastParagraphIndex = :paragraphIndex, lastReadAt = :timestamp WHERE documentId = :documentId")
    suspend fun updateReadingPosition(documentId: Long, page: Int, paragraphIndex: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Actualiza la configuración de TTS
     */
    @Query("UPDATE reading_progress SET ttsSpeed = :speed, ttsPitch = :pitch WHERE documentId = :documentId")
    suspend fun updateTtsSettings(documentId: Long, speed: Float, pitch: Float)

    /**
     * Elimina el progreso de lectura para un documento
     */
    @Query("DELETE FROM reading_progress WHERE documentId = :documentId")
    suspend fun deleteReadingProgress(documentId: Long)
}
