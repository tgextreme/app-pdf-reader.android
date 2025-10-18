package gonzalez.tomas.pdfreadertomas.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un documento PDF en la base de datos
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val title: String,
    val author: String? = null,
    val pageCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long? = null,
    val lastPage: Int = 0,
    val progressFloat: Float = 0f,
    val coverPath: String? = null,
    val hasText: Boolean = true,
    val languageHint: String? = null
)
