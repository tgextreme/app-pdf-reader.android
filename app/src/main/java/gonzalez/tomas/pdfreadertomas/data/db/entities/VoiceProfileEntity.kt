package gonzalez.tomas.pdfreadertomas.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un perfil de voz para TTS
 */
@Entity(tableName = "voice_profiles")
data class VoiceProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val engine: String, // Motor TTS a utilizar
    val voiceName: String, // Nombre de la voz
    val locale: String, // Localización (ej: "es-ES", "en-US")
    val speed: Float = 1.0f, // Velocidad de reproducción
    val pitch: Float = 1.0f, // Tono de voz
    val isDefault: Boolean = false // Indica si es el perfil predeterminado
)
