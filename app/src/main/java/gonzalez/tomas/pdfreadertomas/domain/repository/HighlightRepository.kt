package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.Highlight
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar resaltados de texto
 */
interface HighlightRepository {
    /**
     * Obtiene todos los resaltados para un documento
     */
    fun getHighlightsForDocumentFlow(documentId: Long): Flow<List<Highlight>>

    /**
     * Obtiene los resaltados para una página específica de un documento
     */
    fun getHighlightsForPageFlow(documentId: Long, page: Int): Flow<List<Highlight>>

    /**
     * Obtiene un resaltado específico por su ID
     */
    suspend fun getHighlightById(id: Long): Highlight?

    /**
     * Inserta un nuevo resaltado
     * @return ID del resaltado insertado
     */
    suspend fun insertHighlight(highlight: Highlight): Long

    /**
     * Actualiza un resaltado existente
     */
    suspend fun updateHighlight(highlight: Highlight)

    /**
     * Elimina un resaltado
     */
    suspend fun deleteHighlight(id: Long)

    /**
     * Elimina todos los resaltados de una página específica
     */
    suspend fun deleteHighlightsByPage(documentId: Long, page: Int)

    /**
     * Elimina todos los resaltados de un documento
     */
    suspend fun deleteAllHighlightsForDocument(documentId: Long)

    /**
     * Busca resaltados por texto (útil para búsqueda en documentos)
     */
    suspend fun searchHighlightsByText(documentId: Long, text: String): List<Highlight>
}
