package gonzalez.tomas.pdfreadertomas.ui.screens.lector.tts

import android.speech.tts.Voice
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Locale

/**
 * Diálogo para seleccionar una voz TTS entre las disponibles
 */
@Composable
fun VoiceSelectionDialog(
    availableVoices: List<Voice>,
    currentVoice: Voice?,
    isLoading: Boolean,
    onVoiceSelected: (Voice) -> Unit,
    onDismiss: () -> Unit,
    onTestVoice: (Voice) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Seleccionar voz",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Lista de voces disponibles
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (availableVoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay voces disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        // Agrupar voces por idioma y mostrarlas organizadas
                        val voicesByLanguage = availableVoices.groupBy { it.locale }

                        voicesByLanguage.forEach { (locale, voicesForLocale) ->
                            item {
                                // Encabezado del idioma
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    tonalElevation = 1.dp
                                ) {
                                    Text(
                                        text = getLanguageName(locale),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                                    )
                                }
                            }

                            items(voicesForLocale) { voice ->
                                VoiceItem(
                                    voice = voice,
                                    isSelected = voice == currentVoice,
                                    onVoiceSelected = { onVoiceSelected(voice) },
                                    onTestVoice = { onTestVoice(voice) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceItem(
    voice: Voice,
    isSelected: Boolean,
    onVoiceSelected: () -> Unit,
    onTestVoice: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onVoiceSelected
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onVoiceSelected
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getVoiceName(voice),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = getVoiceQuality(voice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onTestVoice,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Probar voz",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Obtiene el nombre legible de un idioma a partir del Locale
 */
private fun getLanguageName(locale: Locale): String {
    return try {
        val displayName = locale.getDisplayName(Locale.getDefault())
        if (displayName.isNotEmpty()) {
            displayName.replaceFirstChar { it.uppercase() }
        } else {
            locale.language.uppercase()
        }
    } catch (e: Exception) {
        locale.language.uppercase()
    }
}

/**
 * Obtiene un nombre amigable para la voz
 */
private fun getVoiceName(voice: Voice): String {
    val name = voice.name

    return when {
        name.contains("female", ignoreCase = true) -> "Femenina"
        name.contains("male", ignoreCase = true) -> "Masculina"
        else -> {
            // Eliminar prefijos comunes de los nombres de voz
            name.replace(Regex("^(en_US-|es_ES-|com\\.google\\.android\\.tts\\.)*"), "")
                .replace("_", " ")
        }
    }
}

/**
 * Determina la calidad de la voz según sus características
 */
private fun getVoiceQuality(voice: Voice): String {
    return when {
        voice.quality > Voice.QUALITY_NORMAL -> "Calidad alta"
        voice.isNetworkConnectionRequired -> "Requiere red"
        else -> "Calidad normal"
    }
}
