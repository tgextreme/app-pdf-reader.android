package gonzalez.tomas.pdfreadertomas.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que registra el progreso de lectura de un documento PDF
 * Incluye posición exacta (página y párrafo) y configuraciones de TTS
 */
@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")],
    primaryKeys = ["documentId"]
)
data class ReadingProgressEntity(
    val documentId: Long,
    val lastPage: Int = 0,
    val lastParagraphIndex: Int = 0,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val lastReadAt: Long = System.currentTimeMillis()
)
