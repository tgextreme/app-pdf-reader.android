package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar el progreso de lectura
 */
interface ReadingProgressRepository {
    /**
     * Obtiene el progreso de lectura para un documento específico
     */
    suspend fun getReadingProgressForDocument(documentId: Long): ReadingProgress?

    /**
     * Observa el progreso de lectura para un documento mediante Flow
     */
    fun getReadingProgressForDocumentFlow(documentId: Long): Flow<ReadingProgress?>

    /**
     * Inserta o actualiza el progreso de lectura para un documento
     */
    suspend fun insertOrUpdateReadingProgress(readingProgress: ReadingProgress)

    /**
     * Actualiza la posición de lectura
     */
    suspend fun updateReadingPosition(documentId: Long, page: Int, paragraphIndex: Int)

    /**
     * Actualiza la configuración de TTS
     */
    suspend fun updateTtsSettings(documentId: Long, speed: Float, pitch: Float)

    /**
     * Elimina el progreso de lectura para un documento
     */
    suspend fun deleteReadingProgress(documentId: Long)
}
