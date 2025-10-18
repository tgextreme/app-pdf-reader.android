package gonzalez.tomas.pdfreadertomas.domain.model

/**
 * Modelo de dominio para un perfil de voz para TTS
 */
data class VoiceProfile(
    val id: Long = 0,
    val engine: String, // Motor TTS a utilizar
    val voiceName: String, // Nombre de la voz
    val locale: String, // Localización (ej: "es-ES", "en-US")
    val speed: Float = 1.0f, // Velocidad de reproducción
    val pitch: Float = 1.0f, // Tono de voz
    val isDefault: Boolean = false // Indica si es el perfil predeterminado
)
