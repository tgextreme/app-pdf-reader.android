package gonzalez.tomas.pdfreadertomas.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gonzalez.tomas.pdfreadertomas.data.db.entities.VoiceProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con perfiles de voz TTS
 */
@Dao
interface VoiceProfileDao {
    /**
     * Obtiene todos los perfiles de voz
     */
    @Query("SELECT * FROM voice_profiles ORDER BY isDefault DESC, voiceName ASC")
    fun getAllVoiceProfilesFlow(): Flow<List<VoiceProfileEntity>>

    /**
     * Obtiene un perfil de voz específico por su ID
     */
    @Query("SELECT * FROM voice_profiles WHERE id = :profileId")
    suspend fun getVoiceProfileById(profileId: Long): VoiceProfileEntity?

    /**
     * Obtiene el perfil de voz predeterminado
     */
    @Query("SELECT * FROM voice_profiles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultVoiceProfile(): VoiceProfileEntity?

    /**
     * Obtiene el perfil de voz predeterminado como Flow
     */
    @Query("SELECT * FROM voice_profiles WHERE isDefault = 1 LIMIT 1")
    fun getDefaultVoiceProfileFlow(): Flow<VoiceProfileEntity?>

    /**
     * Inserta un perfil de voz y devuelve su ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceProfile(voiceProfile: VoiceProfileEntity): Long

    /**
     * Actualiza un perfil de voz existente
     */
    @Update
    suspend fun updateVoiceProfile(voiceProfile: VoiceProfileEntity)

    /**
     * Elimina un perfil de voz
     */
    @Delete
    suspend fun deleteVoiceProfile(voiceProfile: VoiceProfileEntity)

    /**
     * Establece un perfil de voz como predeterminado y desmarca cualquier otro
     */
    @Query("UPDATE voice_profiles SET isDefault = (id = :profileId)")
    suspend fun setDefaultVoiceProfile(profileId: Long)

    /**
     * Busca perfiles de voz por nombre o locale
     */
    @Query("SELECT * FROM voice_profiles WHERE voiceName LIKE '%' || :query || '%' OR locale LIKE '%' || :query || '%'")
    fun searchVoiceProfilesFlow(query: String): Flow<List<VoiceProfileEntity>>
}
