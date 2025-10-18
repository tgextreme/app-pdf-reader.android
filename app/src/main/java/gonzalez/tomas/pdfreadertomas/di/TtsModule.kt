package gonzalez.tomas.pdfreadertomas.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gonzalez.tomas.pdfreadertomas.pdf.renderer.PdfRendererWrapper
import gonzalez.tomas.pdfreadertomas.tts.engine.TtsEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {

    @Provides
    @Singleton
    fun provideTtsEngine(@ApplicationContext context: Context): TtsEngine {
        return TtsEngine(context)
    }

    @Provides
    @Singleton
    fun providePdfRenderer(@ApplicationContext context: Context): PdfRendererWrapper {
        return PdfRendererWrapper(context)
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("app_preferences") }
        )
    }
}
