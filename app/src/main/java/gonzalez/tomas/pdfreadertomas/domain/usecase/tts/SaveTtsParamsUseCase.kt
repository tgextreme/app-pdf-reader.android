package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject

class SaveTtsParamsUseCase @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val TTS_SPEED = floatPreferencesKey("tts_speed")
        private val TTS_PITCH = floatPreferencesKey("tts_pitch")
        private val TTS_LOCALE = stringPreferencesKey("tts_locale")
        private val TTS_VOICE_NAME = stringPreferencesKey("tts_voice_name")
        private val TTS_ENGINE = stringPreferencesKey("tts_engine")
    }

    /**
     * Guarda la velocidad de reproducción TTS
     */
    suspend fun saveSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[TTS_SPEED] = speed
        }
    }

    /**
     * Guarda el tono de reproducción TTS
     */
    suspend fun savePitch(pitch: Float) {
        dataStore.edit { preferences ->
            preferences[TTS_PITCH] = pitch
        }
    }

    /**
     * Guarda el idioma para TTS
     */
    suspend fun saveLocale(locale: String) {
        dataStore.edit { preferences ->
            preferences[TTS_LOCALE] = locale
        }
    }

    /**
     * Guarda el nombre de la voz TTS
     */
    suspend fun saveVoiceName(voiceName: String) {
        dataStore.edit { preferences ->
            preferences[TTS_VOICE_NAME] = voiceName
        }
    }

    /**
     * Guarda el motor TTS preferido
     */
    suspend fun saveEngine(engine: String) {
        dataStore.edit { preferences ->
            preferences[TTS_ENGINE] = engine
        }
    }
}
