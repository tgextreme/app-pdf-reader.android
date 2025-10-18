package gonzalez.tomas.pdfreadertomas.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gonzalez.tomas.pdfreadertomas.data.db.PdfReaderDatabase
import gonzalez.tomas.pdfreadertomas.data.db.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePdfReaderDatabase(@ApplicationContext context: Context): PdfReaderDatabase {
        return Room.databaseBuilder(
            context,
            PdfReaderDatabase::class.java,
            PdfReaderDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDocumentDao(database: PdfReaderDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: PdfReaderDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideHighlightDao(database: PdfReaderDatabase): HighlightDao {
        return database.highlightDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: PdfReaderDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideReadingProgressDao(database: PdfReaderDatabase): ReadingProgressDao {
        return database.readingProgressDao()
    }

    @Provides
    @Singleton
    fun provideCollectionDao(database: PdfReaderDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    @Singleton
    fun provideVoiceProfileDao(database: PdfReaderDatabase): VoiceProfileDao {
        return database.voiceProfileDao()
    }
}
