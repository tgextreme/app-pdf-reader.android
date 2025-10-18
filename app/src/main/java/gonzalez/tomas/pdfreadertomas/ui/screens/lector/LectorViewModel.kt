package gonzalez.tomas.pdfreadertomas.ui.screens.lector

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.domain.model.Bookmark
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.model.Highlight
import gonzalez.tomas.pdfreadertomas.domain.model.ReadingProgress
import gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark.AddBookmarkUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark.GetBookmarksForDocumentUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark.RemoveBookmarkUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.document.GetDocumentUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.highlight.AddHighlightUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.highlight.GetHighlightsForDocumentUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.reading.GetReadingProgressUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.reading.UpdateReadingProgressUseCase
import gonzalez.tomas.pdfreadertomas.pdf.renderer.PdfRendererWrapper
import gonzalez.tomas.pdfreadertomas.tts.model.PdfParagraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LectorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfRenderer: PdfRendererWrapper,
    private val getDocumentUseCase: GetDocumentUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val getBookmarksForDocumentUseCase: GetBookmarksForDocumentUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    private val getHighlightsForDocumentUseCase: GetHighlightsForDocumentUseCase,
    private val addHighlightUseCase: AddHighlightUseCase
) : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow(LectorUiState())
    val uiState: StateFlow<LectorUiState> = _uiState.asStateFlow()

    // Bitmap de la página actual
    private val _currentPageBitmap = MutableStateFlow<Bitmap?>(null)
    val currentPageBitmap: StateFlow<Bitmap?> = _currentPageBitmap.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Párrafo actualmente en reproducción TTS
    private val _currentTtsParagraph = MutableStateFlow<PdfParagraph?>(null)
    val currentTtsParagraph: StateFlow<PdfParagraph?> = _currentTtsParagraph.asStateFlow()

    // Marcadores para el documento actual
    private var documentId: Long = -1
    val bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())

    // Obtener si la página actual está marcada
    val isCurrentPageBookmarked = combine(
        bookmarks,
        uiState.map { it.currentPage }
    ) { bookmarkList, currentPage ->
        bookmarkList.any { it.page == currentPage }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Carga un documento PDF por su ID
     */
    fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            this@LectorViewModel.documentId = documentId

            try {
                // Obtener documento
                val document = getDocumentUseCase(documentId)
                document?.let {
                    // Configurar UI con datos del documento
                    _uiState.update { state ->
                        state.copy(
                            document = document,
                            pageCount = document.pageCount,
                            isDocumentLoaded = true
                        )
                    }

                    // Abrir documento en el renderer
                    val uri = Uri.parse(document.uri)
                    val opened = pdfRenderer.openDocument(uri)
                    if (opened) {
                        // Cargar progreso de lectura
                        val readingProgress = getReadingProgressUseCase(documentId)
                        val initialPage = readingProgress?.lastPage ?: 0

                        // Actualizar estado con página inicial
                        _uiState.update { state ->
                            state.copy(
                                currentPage = initialPage,
                                readingProgress = readingProgress
                            )
                        }

                        // Renderizar página inicial
                        renderCurrentPage()

                        // Cargar marcadores
                        loadBookmarks(documentId)
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                error = "No se pudo abrir el documento"
                            )
                        }
                    }
                } ?: run {
                    _uiState.update { state ->
                        state.copy(
                            error = "Documento no encontrado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error: ${e.localizedMessage}"
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Renderiza la página actual del documento
     */
    fun renderCurrentPage(scale: Float = 1f) {
        val currentPage = _uiState.value.currentPage

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val bitmap = pdfRenderer.renderPage(
                    pageIndex = currentPage,
                    width = (1080 * scale).toInt(), // Resolución base, ajustar según necesidades
                    highlightParagraph = _currentTtsParagraph.value
                )

                _currentPageBitmap.value = bitmap

                // Actualizar progreso de lectura
                updateReadingProgress(currentPage)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error al renderizar: ${e.localizedMessage}"
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cambia a la página especificada
     */
    fun goToPage(page: Int) {
        if (page < 0 || page >= (_uiState.value.pageCount ?: 0)) {
            return
        }

        _uiState.update { state ->
            state.copy(currentPage = page)
        }

        renderCurrentPage()
    }

    /**
     * Siguiente página
     */
    fun nextPage() {
        goToPage(_uiState.value.currentPage + 1)
    }

    /**
     * Página anterior
     */
    fun previousPage() {
        goToPage(_uiState.value.currentPage - 1)
    }

    /**
     * Actualiza el progreso de lectura en la base de datos
     */
    private fun updateReadingProgress(page: Int) {
        val documentId = documentId
        if (documentId != -1L) {
            viewModelScope.launch {
                val currentProgress = _uiState.value.readingProgress
                val updatedProgress = currentProgress?.copy(
                    lastPage = page
                ) ?: ReadingProgress(
                    documentId = documentId,
                    lastPage = page,
                    lastParagraphIndex = 0
                )

                updateReadingProgressUseCase(updatedProgress)

                _uiState.update { state ->
                    state.copy(readingProgress = updatedProgress)
                }
            }
        }
    }

    /**
     * Carga los marcadores del documento
     */
    private fun loadBookmarks(documentId: Long) {
        viewModelScope.launch {
            val documentBookmarks = getBookmarksForDocumentUseCase(documentId)
            bookmarks.value = documentBookmarks
        }
    }

    /**
     * Alterna el marcador en la página actual
     */
    fun toggleBookmark() {
        viewModelScope.launch {
            val currentPage = _uiState.value.currentPage
            val isBookmarked = isCurrentPageBookmarked.value

            if (isBookmarked) {
                // Buscar y eliminar el marcador
                val bookmark = bookmarks.value.find { it.page == currentPage }
                bookmark?.let {
                    removeBookmarkUseCase(it.id)
                    loadBookmarks(documentId) // Recargar lista
                }
            } else {
                // Añadir nuevo marcador
                val newBookmark = Bookmark(
                    id = UUID.randomUUID().toString(),
                    documentId = documentId,
                    page = currentPage,
                    createdAt = System.currentTimeMillis(),
                    note = null
                )
                addBookmarkUseCase(newBookmark)
                loadBookmarks(documentId) // Recargar lista
            }
        }
    }

    /**
     * Establece el párrafo actual para TTS (para resaltado)
     */
    fun setCurrentTtsParagraph(paragraph: PdfParagraph?) {
        _currentTtsParagraph.value = paragraph
        // Si el párrafo es de otra página, cambiar a esa página
        paragraph?.let {
            if (it.pageNumber != _uiState.value.currentPage) {
                goToPage(it.pageNumber)
            } else {
                // Rerenderizar la página actual para mostrar el resaltado
                renderCurrentPage()
            }
        }
    }

    override fun onCleared() {
        pdfRenderer.closeDocument()
        super.onCleared()
    }
}

/**
 * Estado de la UI del lector
 */
data class LectorUiState(
    val document: Document? = null,
    val currentPage: Int = 0,
    val pageCount: Int? = null,
    val readingProgress: ReadingProgress? = null,
    val isDocumentLoaded: Boolean = false,
    val error: String? = null
)
