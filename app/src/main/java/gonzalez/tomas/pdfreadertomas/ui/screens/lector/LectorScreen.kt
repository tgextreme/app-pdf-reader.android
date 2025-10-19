package gonzalez.tomas.pdfreadertomas.ui.screens.lector

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import gonzalez.tomas.pdfreadertomas.ui.screens.lector.components.PdfViewer
import gonzalez.tomas.pdfreadertomas.ui.screens.lector.tts.TtsControls
import gonzalez.tomas.pdfreadertomas.ui.screens.lector.tts.TtsViewModel
import gonzalez.tomas.pdfreadertomas.ui.screens.lector.tts.VoiceSelectionDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectorScreen(
    documentId: Long,
    onNavigateBack: () -> Unit,
    lectorViewModel: LectorViewModel = hiltViewModel(),
    ttsViewModel: TtsViewModel = hiltViewModel()
) {
    val uiState by lectorViewModel.uiState.collectAsState()
    // Ya no necesitamos colectar estos estados individualmente, están en uiState
    val isBookmarked = uiState.hasBookmarkInCurrentPage
    val ttsState by ttsViewModel.ttsUiState.collectAsState()
    val sleepTimerState by ttsViewModel.sleepTimerState.collectAsState()
    val voicesUiState by ttsViewModel.voicesUiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Controlar visibilidad de barras y controles
    var showControls by remember { mutableStateOf(true) }
    var showTtsControls by remember { mutableStateOf(false) }
    var showVoiceSelectionDialog by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    // Auto-ocultar controles después de un periodo de inactividad
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Cargar documento cuando se inicia la pantalla
    LaunchedEffect(documentId) {
        lectorViewModel.loadDocument(documentId)
    }

    // Observar el párrafo actual de TTS para sincronizar resaltado
    LaunchedEffect(ttsState.currentParagraph) {
        ttsState.currentParagraph?.let {
            lectorViewModel.setCurrentTtsParagraph(it)
        }
    }

    // Diálogo de selección de voces
    if (showVoiceSelectionDialog) {
        VoiceSelectionDialog(
            availableVoices = voicesUiState.availableVoices,
            currentVoice = voicesUiState.currentVoice,
            isLoading = voicesUiState.isLoading,
            onVoiceSelected = {
                ttsViewModel.selectVoice(it)
                // No cerramos el diálogo para permitir probar diferentes voces
            },
            onDismiss = { showVoiceSelectionDialog = false },
            onTestVoice = { ttsViewModel.testVoice(it) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.document?.title ?: "Lector PDF",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        // Botón de marcador
                        IconButton(onClick = { lectorViewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Quitar marcador" else "Añadir marcador"
                            )
                        }

                        // Botón TTS
                        IconButton(
                            onClick = {
                                showTtsControls = !showTtsControls
                                if (showTtsControls && uiState.document != null) {
                                    ttsViewModel.startReading(
                                        uiState.document!!,
                                        uiState.currentPage,
                                        uiState.currentPageBitmap
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Leer en voz alta"
                            )
                        }

                        // Menú de opciones
                        Box {
                            IconButton(onClick = { showDropdownMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }

                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ir a página...") },
                                    onClick = {
                                        // TODO: Implementar diálogo para ir a página específica
                                        showDropdownMenu = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Ver marcadores") },
                                    onClick = {
                                        // TODO: Implementar diálogo para mostrar marcadores
                                        showDropdownMenu = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Visor de PDF
            if (uiState.isDocumentLoaded) {
                uiState.pageCount?.let { pageCount ->
                    PdfViewer(
                        pageCount = pageCount,
                        onRequestPage = { page, scale ->
                            if (page != uiState.currentPage) {
                                lectorViewModel.goToPage(page)
                            } else if (scale != 1.0f) {
                                lectorViewModel.renderCurrentPage(scale)
                            }
                        },
                        currentPageBitmap = uiState.currentPageBitmap,
                        isLoading = uiState.isLoading,
                        onPageChanged = { page ->
                            lectorViewModel.goToPage(page)
                        },
                        highlightParagraph = null, // Ajustar el tipo correcto o pasar null temporalmente
                        onTap = { showControls = !showControls },
                        initialPage = uiState.currentPage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Controles TTS
            TtsControls(
                ttsState = ttsState,
                sleepTimerState = sleepTimerState,
                voicesUiState = voicesUiState,
                onPlayPause = { ttsViewModel.togglePlayback() },
                onNext = { ttsViewModel.nextParagraph() },
                onPrevious = { ttsViewModel.previousParagraph() },
                onStop = {
                    ttsViewModel.stopTts()
                    showTtsControls = false
                },
                onSpeedChange = { ttsViewModel.setSpeed(it) },
                onStartSleepTimer = { ttsViewModel.startSleepTimer(it) },
                onCancelSleepTimer = { ttsViewModel.cancelSleepTimer() },
                onShowVoiceSelection = { showVoiceSelectionDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                visible = showTtsControls
            )

            // Mostrar errores
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar(error)
                }
            }
        }
    }
}
