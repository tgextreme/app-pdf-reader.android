package gonzalez.tomas.pdfreadertomas.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de relación muchos a muchos entre documentos y colecciones
 */
@Entity(
    tableName = "document_collections",
    primaryKeys = ["documentId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("documentId"),
        Index("collectionId")
    ]
)
data class DocumentCollectionEntity(
    val documentId: Long,
    val collectionId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
