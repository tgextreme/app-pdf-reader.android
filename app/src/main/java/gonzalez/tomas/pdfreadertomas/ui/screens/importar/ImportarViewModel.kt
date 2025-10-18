package gonzalez.tomas.pdfreadertomas.ui.screens.importar

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gonzalez.tomas.pdfreadertomas.domain.usecase.document.ImportPdfDocumentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImportarViewModel @Inject constructor(
    private val importPdfDocumentUseCase: ImportPdfDocumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportarUiState())
    val uiState: StateFlow<ImportarUiState> = _uiState.asStateFlow()

    fun processPdfDocument(uri: Uri, cacheDir: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = importPdfDocumentUseCase(uri, cacheDir)

                result.fold(
                    onSuccess = { document ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                importSuccess = true,
                                processedFiles = it.processedFiles + 1,
                                lastImportedDocument = document
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Error al importar PDF"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al importar PDF"
                    )
                }
            }
        }
    }

    fun processPdfFolder(folderUri: Uri, cacheDir: File) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    processedFiles = 0,
                    totalFiles = 0
                )
            }

            // Nota: Para simplificar, este es un placeholder.
            // La implementación real requeriría acceso al ContentResolver para listar documentos
            // desde un árbol de documentos SAF (Storage Access Framework)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "La importación de carpetas se implementará próximamente"
                )
            }
        }
    }

    fun resetState() {
        _uiState.update {
            ImportarUiState(
                processedFiles = it.processedFiles,
                totalFiles = it.totalFiles
            )
        }
    }
}

data class ImportarUiState(
    val isLoading: Boolean = false,
    val importSuccess: Boolean = false,
    val processedFiles: Int = 0,
    val totalFiles: Int = 0,
    val progress: Int = 0,
    val error: String? = null,
    val lastImportedDocument: gonzalez.tomas.pdfreadertomas.domain.model.Document? = null
)
