package gonzalez.tomas.pdfreadertomas.ui.screens.lector

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectorPdfScreen(
    viewModel: LectorViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // Control de visibilidad de las barras de herramientas
    var showControls by remember { mutableStateOf(true) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(message = error)
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = { Text(uiState.documentTitle) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        // Botón de marcadores
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            val icon = if (uiState.hasBookmarkInCurrentPage) {
                                Icons.Default.Bookmark
                            } else {
                                Icons.Default.BookmarkBorder
                            }
                            Icon(icon, contentDescription = "Marcador")
                        }

                        // Botón de opciones
                        IconButton(onClick = { /* Implementar menú de opciones */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BottomAppBar {
                    // Información de página
                    Text(
                        text = "Página ${uiState.currentPage + 1}/${uiState.pageCount}",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Controles TTS
                    Row {
                        // Botón reproducir/pausar
                        if (uiState.isTtsPlaying) {
                            IconButton(onClick = { viewModel.pauseTts() }) {
                                Icon(Icons.Default.Pause, contentDescription = "Pausar")
                            }
                        } else {
                            IconButton(onClick = { viewModel.startTts() }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Leer")
                            }
                        }

                        // Botón ajustes TTS
                        IconButton(onClick = { /* Implementar ajustes TTS */ }) {
                            Icon(Icons.Default.Speed, contentDescription = "Velocidad TTS")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Renderizado de la página PDF
            uiState.currentPageBitmap?.let { bitmap ->
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Página ${uiState.currentPage + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { showControls = !showControls }
                            )
                        }
                )
            }

            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Controles para navegación entre páginas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón página anterior
                if (uiState.currentPage > 0) {
                    FloatingActionButton(
                        onClick = { viewModel.goToPreviousPage() },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Icon(
                            Icons.Default.NavigateBefore,
                            contentDescription = "Página anterior"
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Botón página siguiente
                if (uiState.currentPage < uiState.pageCount - 1) {
                    FloatingActionButton(
                        onClick = { viewModel.goToNextPage() },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Icon(
                            Icons.Default.NavigateNext,
                            contentDescription = "Página siguiente"
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Indicador de TTS activo
            if (uiState.isTtsPlaying && uiState.currentTtsParagraph.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showControls) 80.dp else 16.dp, start = 16.dp, end = 16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = uiState.currentTtsParagraph,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
