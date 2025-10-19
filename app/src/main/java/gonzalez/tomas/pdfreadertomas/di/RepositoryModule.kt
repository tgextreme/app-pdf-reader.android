package gonzalez.tomas.pdfreadertomas.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gonzalez.tomas.pdfreadertomas.data.repository.BookmarkRepositoryImpl
import gonzalez.tomas.pdfreadertomas.data.repository.DocumentRepositoryImpl
import gonzalez.tomas.pdfreadertomas.data.repository.ReadingProgressRepositoryImpl
import gonzalez.tomas.pdfreadertomas.domain.repository.BookmarkRepository
import gonzalez.tomas.pdfreadertomas.domain.repository.DocumentRepository
import gonzalez.tomas.pdfreadertomas.domain.repository.ReadingProgressRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideReadingProgressRepository(impl: ReadingProgressRepositoryImpl): ReadingProgressRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }
}
