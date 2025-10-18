package gonzalez.tomas.pdfreadertomas.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import gonzalez.tomas.pdfreadertomas.data.db.dao.*
import gonzalez.tomas.pdfreadertomas.data.db.entities.*

/**
 * Base de datos principal del lector PDF con TTS
 */
@Database(
    entities = [
        DocumentEntity::class,
        BookmarkEntity::class,
        HighlightEntity::class,
        NoteEntity::class,
        ReadingProgressEntity::class,
        CollectionEntity::class,
        DocumentCollectionEntity::class,
        VoiceProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class PdfReaderDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun collectionDao(): CollectionDao
    abstract fun voiceProfileDao(): VoiceProfileDao

    companion object {
        const val DATABASE_NAME = "pdf_reader_database"
    }
}
