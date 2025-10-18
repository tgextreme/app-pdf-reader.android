package gonzalez.tomas.pdfreadertomas.di

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gonzalez.tomas.pdfreadertomas.pdf.extractor.PdfTextExtractor
import gonzalez.tomas.pdfreadertomas.pdf.renderer.PdfRendererWrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PdfModule {

    @Provides
    @Singleton
    fun providePdfRendererWrapper(@ApplicationContext context: Context): PdfRendererWrapper {
        return PdfRendererWrapper(context)
    }

    @Provides
    @Singleton
    fun providePdfTextExtractor(@ApplicationContext context: Context): PdfTextExtractor {
        // Inicializar PDFBox
        PDFBoxResourceLoader.init(context)
        return PdfTextExtractor(context)
    }
}
