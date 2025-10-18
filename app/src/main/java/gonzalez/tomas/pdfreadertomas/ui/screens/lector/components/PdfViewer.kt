package gonzalez.tomas.pdfreadertomas.ui.screens.lector.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import gonzalez.tomas.pdfreadertomas.tts.model.PdfParagraph
import kotlin.math.max
import kotlin.math.min

/**
 * Componente para visualizar un documento PDF con zoom y gestos
 */
@Composable
fun PdfViewer(
    pageCount: Int,
    onRequestPage: (Int, Float) -> Unit,
    currentPageBitmap: Bitmap?,
    isLoading: Boolean,
    onPageChanged: (Int) -> Unit,
    highlightParagraph: PdfParagraph? = null,
    onTap: () -> Unit = {},
    onDoubleTap: (Int, Float, Float) -> Unit = { _, _, _ -> },
    initialPage: Int = 0,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }

    // Escala para el zoom
    var scale by remember { mutableFloatStateOf(1f) }
    // Centro del zoom
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Observar cambios de página
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            // Resetear el zoom al cambiar de página
            scale = 1f
            offsetX = 0f
            offsetY = 0f
            onPageChanged(page)
        }
    }

    // Solicitar la página actual cuando cambia la escala
    LaunchedEffect(pagerState.currentPage, scale) {
        onRequestPage(pagerState.currentPage, scale)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val screenWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val screenHeight = with(LocalDensity.current) { maxHeight.toPx() }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onTap() },
                            onDoubleTap = { offset ->
                                onDoubleTap(page, offset.x, offset.y)
                                // Alternar entre zoom 1x y 2x en doble tap
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2f
                                    // Centrar el zoom en el punto del doble tap
                                    offsetX = (screenWidth / 2 - offset.x) * scale
                                    offsetY = (screenHeight / 2 - offset.y) * scale
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Limitar el zoom entre 1x y 5x
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            // Aplicar el paneo solo si hay zoom
                            if (scale > 1f) {
                                // Calcular límites de desplazamiento
                                val maxX = (screenWidth * (scale - 1)) / 2
                                val maxY = (screenHeight * (scale - 1)) / 2

                                // Aplicar el desplazamiento con límites
                                offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                            } else {
                                // Sin zoom, reset de desplazamiento
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading && currentPageBitmap == null) {
                    CircularProgressIndicator()
                } else if (currentPageBitmap != null && page == pagerState.currentPage) {
                    Image(
                        bitmap = currentPageBitmap.asImageBitmap(),
                        contentDescription = "Página PDF ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                } else {
                    Text(
                        text = "Página ${page + 1}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Indicador de página actual
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / $pageCount",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
