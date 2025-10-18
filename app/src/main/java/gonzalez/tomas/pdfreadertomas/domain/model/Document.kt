package gonzalez.tomas.pdfreadertomas.domain.model

import java.util.Date

/**
 * Modelo de dominio para un documento PDF
 */
data class Document(
    val id: Long = 0,
    val uri: String,
    val title: String,
    val author: String? = null,
    val pageCount: Int = 0,
    val addedAt: Date = Date(),
    val lastOpenedAt: Date? = null,
    val lastPage: Int = 0,
    val progressFloat: Float = 0f,
    val coverPath: String? = null,
    val hasText: Boolean = true,
    val languageHint: String? = null
)
