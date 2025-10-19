package gonzalez.tomas.pdfreadertomas.ui.screens.lector

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.domain.model.Bookmark
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.model.ReadingProgress
import gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark.AddBookmarkUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark.DeleteBookmarkUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.bookmark.GetBookmarksForDocumentUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.document.GetDocumentByIdUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.reading.GetReadingProgressUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.reading.UpdateReadingProgressUseCase
import gonzalez.tomas.pdfreadertomas.pdf.renderer.PdfRendererWrapper
import gonzalez.tomas.pdfreadertomas.tts.model.PdfParagraph
import gonzalez.tomas.pdfreadertomas.tts.model.TtsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LectorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfRenderer: PdfRendererWrapper,
    private val getDocumentByIdUseCase: GetDocumentByIdUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val getBookmarksForDocumentUseCase: GetBookmarksForDocumentUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase
) : ViewModel() {

    // TTS State (podría venir de un servicio TTS)
    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    // Estado de la UI
    private val _uiState = MutableStateFlow(LectorUiState())

    // Bitmap de la página actual
    private val _currentPageBitmap = MutableStateFlow<Bitmap?>(null)

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)

    // Párrafo actualmente en reproducción TTS
    private val _currentTtsParagraph = MutableStateFlow<PdfParagraph?>(null)

    // Marcadores para el documento actual
    private var documentId: Long = -1
    val bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())

    // Obtener si la página actual está marcada
    val isCurrentPageBookmarked = combine(
        bookmarks,
        _uiState.map { it.currentPage }
    ) { bookmarkList, currentPage ->
        bookmarkList.any { it.page == currentPage }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Combina todos los estados en uno solo para la UI
    val uiState: StateFlow<LectorUiState> = combine(
        _uiState,
        _currentPageBitmap,
        _isLoading,
        isCurrentPageBookmarked,
        _ttsState
    ) { state, bitmap, loading, bookmarked, tts ->
        state.copy(
            currentPageBitmap = bitmap,
            isLoading = loading,
            hasBookmarkInCurrentPage = bookmarked,
            isTtsPlaying = tts.isPlaying,
            currentTtsParagraph = tts.currentParagraph?.text ?: ""
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LectorUiState()
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
                val document = getDocumentByIdUseCase(documentId)
                document?.let {
                    // Configurar UI con datos del documento
                    _uiState.update { state ->
                        state.copy(
                            document = document,
                            documentTitle = document.title ?: "",
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
        val docId = documentId
        if (docId != -1L) {
            viewModelScope.launch {
                try {
                    // Llamar al caso de uso con los parámetros separados
                    val result = updateReadingProgressUseCase(
                        documentId = docId,
                        page = page,
                        paragraphIndex = _uiState.value.readingProgress?.lastParagraphIndex ?: 0
                    )

                    // Si la actualización fue exitosa, actualizar el estado local
                    if (result.isSuccess) {
                        val currentProgress = _uiState.value.readingProgress
                        val updatedProgress = currentProgress?.copy(
                            lastPage = page
                        ) ?: ReadingProgress(
                            documentId = docId,
                            lastPage = page,
                            lastParagraphIndex = 0
                        )

                        _uiState.update { state ->
                            state.copy(readingProgress = updatedProgress)
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { state ->
                        state.copy(error = "Error al actualizar progreso: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Carga los marcadores del documento
     */
    private fun loadBookmarks(documentId: Long) {
        viewModelScope.launch {
            try {
                getBookmarksForDocumentUseCase(documentId).collect { documentBookmarks ->
                    bookmarks.value = documentBookmarks
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar marcadores: ${e.message}") }
            }
        }
    }

    /**
     * Alterna el marcador en la página actual
     */
    fun toggleBookmark() {
        viewModelScope.launch {
            val currentPage = _uiState.value.currentPage
            val isBookmarked = isCurrentPageBookmarked.value
            val docId = documentId

            if (isBookmarked) {
                // Buscar y eliminar el marcador
                val bookmark = bookmarks.value.find { it.page == currentPage }
                bookmark?.let {
                    try {
                        deleteBookmarkUseCase(it.id)
                        loadBookmarks(docId) // Recargar lista
                    } catch (e: Exception) {
                        _uiState.update { state ->
                            state.copy(error = "Error al eliminar marcador: ${e.message}")
                        }
                    }
                }
            } else {
                // Añadir nuevo marcador usando parámetros individuales
                try {
                    val result = addBookmarkUseCase(
                        documentId = docId,
                        page = currentPage,
                        note = null
                    )
                    if (result.isSuccess) {
                        loadBookmarks(docId) // Recargar lista
                    } else {
                        _uiState.update { state ->
                            state.copy(error = "No se pudo añadir el marcador")
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { state ->
                        state.copy(error = "Error al añadir marcador: ${e.message}")
                    }
                }
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

    // Añado estos métodos para manejar la navegación que están en la UI
    fun goToNextPage() {
        nextPage()
    }

    fun goToPreviousPage() {
        previousPage()
    }

    // Funcionalidad TTS
    fun startTts() {
        _ttsState.update { it.copy(isPlaying = true) }
        // Aquí iría la lógica real para iniciar el TTS
    }

    fun pauseTts() {
        _ttsState.update { it.copy(isPlaying = false) }
        // Aquí iría la lógica real para pausar el TTS
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
    val documentTitle: String = "",
    val currentPage: Int = 0,
    val pageCount: Int? = null,
    val readingProgress: ReadingProgress? = null,
    val isDocumentLoaded: Boolean = false,
    val hasBookmarkInCurrentPage: Boolean = false,
    val currentPageBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val isTtsPlaying: Boolean = false,
    val currentTtsParagraph: String = "",
    val error: String? = null
)
