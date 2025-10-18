package gonzalez.tomas.pdfreadertomas.domain.usecase.tts

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import gonzalez.tomas.pdfreadertomas.tts.model.TtsParams
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class GetTtsParamsUseCase @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TTS_SPEED = floatPreferencesKey("tts_speed")
        private val TTS_PITCH = floatPreferencesKey("tts_pitch")
        private val TTS_LOCALE = stringPreferencesKey("tts_locale")
        private val TTS_VOICE_NAME = stringPreferencesKey("tts_voice_name")
        private val TTS_ENGINE = stringPreferencesKey("tts_engine")
    }

    /**
     * Obtiene los parámetros de configuración para TTS desde DataStore
     */
    suspend operator fun invoke(): TtsParams {
        return dataStore.data.map { preferences ->
            TtsParams(
                speed = preferences[TTS_SPEED] ?: 1.0f,
                pitch = preferences[TTS_PITCH] ?: 1.0f,
                locale = preferences[TTS_LOCALE] ?: Locale.getDefault().toLanguageTag(),
                voiceName = preferences[TTS_VOICE_NAME],
                engine = preferences[TTS_ENGINE]
            )
        }.first()
    }
}
