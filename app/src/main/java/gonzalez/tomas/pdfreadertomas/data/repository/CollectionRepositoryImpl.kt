package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.CollectionDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.CollectionEntity
import gonzalez.tomas.pdfreadertomas.data.db.entities.DocumentCollectionEntity
import gonzalez.tomas.pdfreadertomas.domain.model.Collection
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao
) : CollectionRepository {

    override fun getAllCollectionsFlow(): Flow<List<Collection>> {
        return collectionDao.getAllCollectionsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCollectionById(collectionId: Long): Collection? {
        val entity = collectionDao.getCollectionById(collectionId) ?: return null
        return entity.toDomain()
    }

    override suspend fun insertCollection(collection: Collection): Long {
        return collectionDao.insertCollection(collection.toEntity())
    }

    override suspend fun updateCollection(collection: Collection) {
        collectionDao.updateCollection(collection.toEntity())
    }

    override suspend fun deleteCollection(id: Long) {
        collectionDao.getCollectionById(id)?.let {
            collectionDao.deleteCollection(it)
        }
    }

    override fun getCollectionsForDocumentFlow(documentId: Long): Flow<List<Collection>> {
        return collectionDao.getCollectionsForDocumentFlow(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDocumentsInCollectionFlow(collectionId: Long): Flow<List<Document>> {
        return collectionDao.getDocumentsInCollectionFlow(collectionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addDocumentToCollection(documentId: Long, collectionId: Long) {
        val documentCollection = DocumentCollectionEntity(
            documentId = documentId,
            collectionId = collectionId,
            addedAt = System.currentTimeMillis()
        )
        collectionDao.addDocumentToCollection(documentCollection)
    }

    override suspend fun removeDocumentFromCollection(documentId: Long, collectionId: Long) {
        collectionDao.removeDocumentFromCollection(documentId, collectionId)
    }

    override suspend fun isDocumentInCollection(documentId: Long, collectionId: Long): Boolean {
        return collectionDao.isDocumentInCollection(documentId, collectionId)
    }

    override suspend fun getDocumentCountInCollection(collectionId: Long): Int {
        return collectionDao.getDocumentCountInCollection(collectionId)
    }

    // Extension functions para mapear entre entidades y modelos de dominio
    private fun CollectionEntity.toDomain(): Collection {
        return Collection(
            id = this.id,
            name = this.name
        )
    }

    private fun Collection.toEntity(): CollectionEntity {
        return CollectionEntity(
            id = this.id,
            name = this.name,
            createdAt = System.currentTimeMillis()
        )
    }

    // Función para convertir DocumentEntity a Document (simplificado)
    private fun gonzalez.tomas.pdfreadertomas.data.db.entities.DocumentEntity.toDomain(): Document {
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
}
