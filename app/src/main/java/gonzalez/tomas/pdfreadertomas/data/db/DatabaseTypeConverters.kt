package gonzalez.tomas.pdfreadertomas.data.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversores de tipos para Room
 */
class DatabaseTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
