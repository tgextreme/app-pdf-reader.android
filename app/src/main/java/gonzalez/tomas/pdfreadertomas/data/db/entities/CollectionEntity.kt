package gonzalez.tomas.pdfreadertomas.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una colección de documentos PDF
 */
@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
