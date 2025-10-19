package gonzalez.tomas.pdfreadertomas.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gonzalez.tomas.pdfreadertomas.tts.service.TtsServiceConnection

/**
 * Punto de entrada para acceder a las dependencias de TTS desde clases que
 * no pueden usar @AndroidEntryPoint, como los BroadcastReceivers
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TtsEntryPoint {

    /**
     * Proporciona acceso a la conexión del servicio TTS
     */
    fun getTtsServiceConnection(): TtsServiceConnection
}
