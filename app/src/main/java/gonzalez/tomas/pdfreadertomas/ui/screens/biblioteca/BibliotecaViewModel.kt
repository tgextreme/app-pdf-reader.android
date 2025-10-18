package gonzalez.tomas.pdfreadertomas.ui.screens.biblioteca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gonzalez.tomas.pdfreadertomas.domain.model.Document
import gonzalez.tomas.pdfreadertomas.domain.usecase.document.GetAllDocumentsFlowUseCase
import gonzalez.tomas.pdfreadertomas.domain.usecase.tts.StartTtsReadingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val getAllDocumentsFlowUseCase: GetAllDocumentsFlowUseCase,
    private val startTtsReadingUseCase: StartTtsReadingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BibliotecaUiState())
    val uiState: StateFlow<BibliotecaUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getAllDocumentsFlowUseCase().collect { documents ->
                    _uiState.update {
                        it.copy(
                            documents = documents,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar documentos"
                    )
                }
            }
        }
    }

    fun startTts(document: Document) {
        viewModelScope.launch {
            startTtsReadingUseCase(
                documentId = document.id,
                documentTitle = document.title,
                uri = document.uri
            )
        }
    }

    fun searchDocuments(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Aquí se podría implementar una búsqueda filtrada de documentos
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
        // Aquí se podría implementar la lógica para reordenar la lista
    }
}

data class BibliotecaUiState(
    val isLoading: Boolean = false,
    val documents: List<Document> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.RECIENTES
)

enum class SortOrder {
    RECIENTES,
    TITULO,
    AUTOR,
    PROGRESO
}
