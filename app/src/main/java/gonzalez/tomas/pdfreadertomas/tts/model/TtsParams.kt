package gonzalez.tomas.pdfreadertomas.tts.model

/**
 * Parámetros de configuración para el servicio TTS
 */
data class TtsParams(
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val locale: String = "es",
    val voiceName: String? = null,
    val engine: String? = null
)
