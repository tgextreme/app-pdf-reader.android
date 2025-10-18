package gonzalez.tomas.pdfreadertomas.ui.screens.lector.tts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gonzalez.tomas.pdfreadertomas.tts.model.TtsState

@Composable
fun TtsControls(
    ttsState: TtsState,
    sleepTimerState: SleepTimerState,
    voicesUiState: VoicesUiState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onStartSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onShowVoiceSelection: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    var showSpeedControl by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var speedValue by remember { mutableFloatStateOf(ttsState.speed) }

    Column(modifier = modifier) {
        // Panel principal de controles TTS
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { it },
            exit = fadeOut(animationSpec = tween(300)) +
                   slideOutVertically(animationSpec = tween(300)) { it }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Encabezado con título e información
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ttsState.currentDocument?.title ?: "Lectura TTS",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = onStop) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar TTS"
                            )
                        }
                    }

                    // Información de progreso
                    Text(
                        text = "Página ${ttsState.currentPage + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    // Párrafo actual (texto que se está leyendo)
                    ttsState.currentParagraph?.let { paragraph ->
                        Text(
                            text = paragraph.text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .alpha(0.7f),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Controles principales: anterior, play/pause, siguiente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TtsControlButton(
                            icon = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Párrafo anterior",
                            onClick = onPrevious
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        // Play/Pause o Cargando
                        if (ttsState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            TtsControlButton(
                                icon = if (ttsState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (ttsState.isPlaying) "Pausar" else "Reproducir",
                                onClick = onPlayPause,
                                isLarge = true
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        TtsControlButton(
                            icon = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Párrafo siguiente",
                            onClick = onNext
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fila de botones adicionales: velocidad, temporizador, voces
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de velocidad
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(onClick = { showSpeedControl = !showSpeedControl }) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Ajustar velocidad"
                                )
                            }
                            Text(
                                text = "${String.format("%.1fx", ttsState.speed)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Botón de temporizador
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BadgedBox(
                                badge = {
                                    if (sleepTimerState.isActive) {
                                        Badge {
                                            Text(sleepTimerState.formattedTime)
                                        }
                                    }
                                }
                            ) {
                                IconButton(onClick = { showSleepTimer = !showSleepTimer }) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Temporizador de sueño"
                                    )
                                }
                            }
                            Text(
                                text = "Temporizador",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Botón de selección de voz
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(onClick = onShowVoiceSelection) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardVoice,
                                    contentDescription = "Seleccionar voz"
                                )
                            }
                            Text(
                                text = "Voces",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Control deslizable de velocidad
                    AnimatedVisibility(visible = showSpeedControl) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "0.5x")
                            Slider(
                                value = speedValue,
                                onValueChange = { speedValue = it },
                                onValueChangeFinished = { onSpeedChange(speedValue) },
                                valueRange = 0.5f..3.0f,
                                steps = 5,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "3.0x")
                        }
                    }

                    // Mostrar error si existe
                    ttsState.error?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Controles del temporizador de sueño
        SleepTimerControls(
            timerState = sleepTimerState,
            onStartTimer = onStartSleepTimer,
            onCancelTimer = onCancelSleepTimer,
            visible = visible && showSleepTimer
        )
    }
}

@Composable
private fun TtsControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val size = if (isLarge) 64.dp else 48.dp

    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primaryContainer,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(if (isLarge) 16.dp else 12.dp)
                .size(if (isLarge) 32.dp else 24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
