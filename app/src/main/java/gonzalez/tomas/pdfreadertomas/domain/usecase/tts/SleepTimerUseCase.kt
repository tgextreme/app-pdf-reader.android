package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Caso de uso para gestionar un temporizador de sueño para el servicio TTS
 */
class SleepTimerUseCase @Inject constructor() {

    /**
     * Inicia un temporizador que emite actualizaciones de tiempo restante
     * y completa cuando el tiempo se agota
     *
     * @param durationMinutes Duración del temporizador en minutos
     * @param updateIntervalSeconds Intervalo de actualización en segundos
     * @return Flow que emite el tiempo restante en segundos
     */
    fun startTimer(durationMinutes: Int, updateIntervalSeconds: Int = 1): Flow<Int> = flow {
        val totalSeconds = durationMinutes * 60
        var remainingSeconds = totalSeconds

        // Emisión inicial
        emit(remainingSeconds)

        while (remainingSeconds > 0) {
            delay(updateIntervalSeconds * 1000L)
            remainingSeconds -= updateIntervalSeconds
            emit(remainingSeconds)
        }
    }

    /**
     * Convierte segundos en formato de tiempo MM:SS
     */
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
