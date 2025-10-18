package gonzalez.tomas.pdfreadertomas.data.repository

import gonzalez.tomas.pdfreadertomas.data.db.dao.VoiceProfileDao
import gonzalez.tomas.pdfreadertomas.data.db.entities.VoiceProfileEntity
import gonzalez.tomas.pdfreadertomas.domain.model.VoiceProfile
import gonzalez.tomas.pdfreadertomas.domain.repository.VoiceProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceProfileRepositoryImpl @Inject constructor(
    private val voiceProfileDao: VoiceProfileDao
) : VoiceProfileRepository {

    override fun getAllVoiceProfilesFlow(): Flow<List<VoiceProfile>> {
        return voiceProfileDao.getAllVoiceProfilesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getVoiceProfileById(profileId: Long): VoiceProfile? {
        val entity = voiceProfileDao.getVoiceProfileById(profileId) ?: return null
        return entity.toDomain()
    }

    override suspend fun getDefaultVoiceProfile(): VoiceProfile? {
        val entity = voiceProfileDao.getDefaultVoiceProfile() ?: return null
        return entity.toDomain()
    }

    override fun getDefaultVoiceProfileFlow(): Flow<VoiceProfile?> {
        return voiceProfileDao.getDefaultVoiceProfileFlow().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertVoiceProfile(voiceProfile: VoiceProfile): Long {
        return voiceProfileDao.insertVoiceProfile(voiceProfile.toEntity())
    }

    override suspend fun updateVoiceProfile(voiceProfile: VoiceProfile) {
        voiceProfileDao.updateVoiceProfile(voiceProfile.toEntity())
    }

    override suspend fun deleteVoiceProfile(id: Long) {
        voiceProfileDao.getVoiceProfileById(id)?.let {
            voiceProfileDao.deleteVoiceProfile(it)
        }
    }

    override suspend fun setDefaultVoiceProfile(profileId: Long) {
        voiceProfileDao.setDefaultVoiceProfile(profileId)
    }

    override fun searchVoiceProfilesFlow(query: String): Flow<List<VoiceProfile>> {
        return voiceProfileDao.searchVoiceProfilesFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Extension functions para mapear entre entidades y modelos de dominio
    private fun VoiceProfileEntity.toDomain(): VoiceProfile {
        return VoiceProfile(
            id = this.id,
            engine = this.engine,
            voiceName = this.voiceName,
            locale = this.locale,
            speed = this.speed,
            pitch = this.pitch,
            isDefault = this.isDefault
        )
    }

    private fun VoiceProfile.toEntity(): VoiceProfileEntity {
        return VoiceProfileEntity(
            id = this.id,
            engine = this.engine,
            voiceName = this.voiceName,
            locale = this.locale,
            speed = this.speed,
            pitch = this.pitch,
            isDefault = this.isDefault
        )
    }
}
