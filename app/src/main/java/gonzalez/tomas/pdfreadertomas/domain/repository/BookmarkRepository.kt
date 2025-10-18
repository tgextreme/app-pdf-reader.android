package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar marcadores
 */
interface BookmarkRepository {
    /**
     * Obtiene todos los marcadores para un documento
     */
    fun getBookmarksForDocumentFlow(documentId: Long): Flow<List<Bookmark>>

    /**
     * Obtiene un marcador específico por su ID
     */
    suspend fun getBookmarkById(id: Long): Bookmark?

    /**
     * Comprueba si existe un marcador para una página específica de un documento
     */
    suspend fun hasBookmarkForPage(documentId: Long, page: Int): Boolean

    /**
     * Inserta un nuevo marcador
     * @return ID del marcador insertado
     */
    suspend fun insertBookmark(bookmark: Bookmark): Long

    /**
     * Actualiza un marcador existente
     */
    suspend fun updateBookmark(bookmark: Bookmark)

    /**
     * Elimina un marcador
     */
    suspend fun deleteBookmark(id: Long)

    /**
     * Elimina un marcador específico por documento y página
     */
    suspend fun deleteBookmarkByPage(documentId: Long, page: Int)

    /**
     * Elimina todos los marcadores de un documento
     */
    suspend fun deleteAllBookmarksForDocument(documentId: Long)

    /**
     * Obtiene el marcador de una página específica de un documento
     */
    suspend fun getBookmarkForPage(documentId: Long, page: Int): Bookmark?
}
