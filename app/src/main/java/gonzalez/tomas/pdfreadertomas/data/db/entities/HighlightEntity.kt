package gonzalez.tomas.pdfreadertomas.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un resaltado de texto en el PDF
 */
@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val page: Int,
    val rect: String, // JSON serializado de los rectángulos que forman el highlight
    val color: Int, // Color ARGB del resaltado
    val createdAt: Long = System.currentTimeMillis(),
    val textOpt: String? = null // Texto resaltado (opcional)
)
