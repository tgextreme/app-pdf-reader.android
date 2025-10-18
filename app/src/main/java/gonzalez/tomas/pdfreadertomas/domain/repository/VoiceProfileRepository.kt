package gonzalez.tomas.pdfreadertomas.domain.repository

import gonzalez.tomas.pdfreadertomas.domain.model.VoiceProfile
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para gestionar perfiles de voz TTS
 */
interface VoiceProfileRepository {
    /**
     * Obtiene todos los perfiles de voz
     */
    fun getAllVoiceProfilesFlow(): Flow<List<VoiceProfile>>

    /**
     * Obtiene un perfil de voz específico por su ID
     */
    suspend fun getVoiceProfileById(profileId: Long): VoiceProfile?

    /**
     * Obtiene el perfil de voz predeterminado
     */
    suspend fun getDefaultVoiceProfile(): VoiceProfile?

    /**
     * Obtiene el perfil de voz predeterminado como Flow
     */
    fun getDefaultVoiceProfileFlow(): Flow<VoiceProfile?>

    /**
     * Inserta un perfil de voz
     * @return ID del perfil insertado
     */
    suspend fun insertVoiceProfile(voiceProfile: VoiceProfile): Long

    /**
     * Actualiza un perfil de voz existente
     */
    suspend fun updateVoiceProfile(voiceProfile: VoiceProfile)

    /**
     * Elimina un perfil de voz
     */
    suspend fun deleteVoiceProfile(id: Long)

    /**
     * Establece un perfil de voz como predeterminado y desmarca cualquier otro
     */
    suspend fun setDefaultVoiceProfile(profileId: Long)

    /**
     * Busca perfiles de voz por nombre o locale
     */
    fun searchVoiceProfilesFlow(query: String): Flow<List<VoiceProfile>>
}
