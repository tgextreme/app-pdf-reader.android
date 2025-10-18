
# 📌 Resumen de producto

App Android (Kotlin + Jetpack Compose + Material 3) para **leer PDFs** y **escucharlos por TTS**. Incluye **biblioteca local**, **marcadores**, **notas**, **resaltados**, **búsqueda**, **modo lectura** y **reproducción en segundo plano** con controles del sistema. Diseño limpio y moderno, enfocado en accesibilidad y rendimiento.

---

# 🎯 Objetivo

* Leer PDFs (paginados) con render nítido.
* Extraer texto del PDF para **leerlo con TTS** por párrafos (con resaltado sincronizado).
* Biblioteca con portadas, progreso y colecciones.
* Experiencia de audio tipo “audiolibro”: notificación, lockscreen, cola de lectura, temporizador de sueño.

---

# 🧱 Alcance de la v1 (MVP)

1. **Importar y gestionar PDFs**

    * Importar por **SAF** (Storage Access Framework): `ACTION_OPEN_DOCUMENT` y `ACTION_OPEN_DOCUMENT_TREE`.
    * Persistir permisos de URI (persistable URI).
    * Miniaturas/portadas generadas por página 1 (cacheadas).
    * Biblioteca: **grid/lista** con filtros, orden (A–Z, recientes, tamaño).
2. **Lector PDF**

    * Render por página con **PdfRenderer** o **AndroidPdfViewer (pdfium)**.
    * Desplazamiento vertical continuo, zoom, salto a página, buscador.
    * **Marcadores**, **resaltados** (sobre bitmap con overlay), **notas**.
    * **Progreso por documento** (última página, porcentaje).
3. **Texto a Voz (TTS)**

    * Motor TTS Android (`TextToSpeech`) con selección de voz, **velocidad** y **tono**.
    * **Extracción de texto**:

        * PDFs con texto: **PDFBox-Android** para extraer por página → dividir en párrafos.
        * PDFs escaneados (sin texto): opción **OCR on-device** (ML Kit Text Recognition v2) por página bajo demanda (desactivado por defecto por rendimiento).
    * **Lectura por párrafos** con **resaltado en tiempo real** (karaoke-like).
    * **Controles de audio del sistema**: notificación persistente, media session, play/pause/next/prev, **temporizador de sueño**.
4. **Diseño / UX**

    * **Material 3** + **Dynamic Color** (Material You).
    * Modo claro/oscuro, **tipografía ajustable** en lector (para UI; el PDF es imagen, el texto resaltado sigue fuente de sistema).
    * Gestos: doble toque para zoom, deslizar para cambiar página, mantener pulsado para seleccionar/escuchar párrafo.
5. **Persistencia**

    * **Room** (Document, Bookmark, Highlight, Note, ReadingProgress, Collection, Tag, VoiceProfile).
    * **DataStore** para preferencias (tema, voz, velocidad, OCR on/off, etc.).

---

# 💡 Ideas “nice to have” (v1.1+)

* **Lista de reproducción** de PDFs (modo estudio).
* **Exportar audio** de TTS a archivo (via `synthesizeToFile`) por capítulo/páginas.
* **Saltos inteligentes**: detectar cabeceras/footers y omitirlos en TTS.
* **Detección automática de idioma** por página (heurística) y selección de voz acorde.
* **Diccionario/Traductor** al seleccionar texto (en PDFs con texto).
* **Estadísticas**: tiempo de lectura, páginas/día, rachas.
* **Respaldo**/restauración de BD (JSON) en Drive/Dropbox (opcional).

---

# 🧭 Flujo de usuario

1. **Onboarding (1ª vez)**

    * Elegir carpeta de libros (SAF) → conceder permiso persistente.
    * Opciones rápidas: voz por defecto, velocidad, tema.
2. **Biblioteca**

    * Tarjetas con portada, título, autor (si metadatos), progreso.
    * Acciones rápidas: reproducir TTS, continuar leyendo, añadir a colección.
3. **Lector**

    * Barra superior: cerrar, info, búsqueda, ir a página.
    * Barra inferior: zoom, marcadores, notas, TTS (play/pausa/velocidad).
    * **Resaltado sincronizado** del párrafo que se narra.
    * **Doble toque** para zoom; pellizcar; deslizar páginas.
4. **Audio en background**

    * Notificación tipo media con controles; **lockscreen**; **temporizador**.

---

# 🏛️ Arquitectura

* **MVVM + Use Cases (Clean-ish)**.
* **Jetpack Compose** para UI, **Navigation Compose**.
* **Hilt** DI.
* **Room** (con Paging 3 para biblioteca grande).
* **Coil** para miniaturas (Bitmaps generados de PdfRenderer).
* **Coroutines/Flow** para estado reactivo.
* **MediaSession** para controlar TTS como si fuera audio.

---

# 🗂️ Modelos (Room)

```text
Document(id, uri:String, title, author, pageCount, addedAt, lastOpenedAt, lastPage, progressFloat, coverPath, hasText:Boolean, languageHint)
Bookmark(id, documentId, page, createdAt, note)
Highlight(id, documentId, page, rect:Json, color, createdAt, textOpt)
Note(id, documentId, page, content, createdAt)
ReadingProgress(documentId PK, lastPage, lastParagraphIndex, ttsSpeed, ttsPitch)
Collection(id, name)
DocumentCollection(documentId, collectionId)
Tag(id, name, color?)
DocumentTag(documentId, tagId)
VoiceProfile(id, engine, voiceName, locale, speed, pitch, isDefault)
```

---

# 🔧 Dependencias sugeridas (realistas)

* UI: `androidx.compose:*`, `material3`, `navigation-compose`
* DI: `com.google.dagger:hilt-android`
* DB: `androidx.room:*`, `paging-runtime`
* Preferencias: `androidx.datastore:datastore-preferences`
* PDF render:

    * Opción A (ligera): **PdfRenderer** del SDK (render bitmap por página).
    * Opción B (lista para producción): **AndroidPdfViewer** (pdfium) con `AndroidView` interop en Compose.
* Extracción de texto: **PDFBox-Android** (`com.tom-roush:pdfbox-android`)
* OCR opcional: **ML Kit Text Recognition v2** (on-device)
* Imágenes: **Coil**
* Permisos: SAF (sin READ_EXTERNAL_STORAGE en Android modernos)

> Nota realista: **PdfRenderer** no extrae texto; por eso usamos PDFBox para TTS. PDFBox es pesado: hacer **caché** de texto por página y hacerlo en **background**. Para PDFs enormes, extraer bajo demanda (páginas próximas a la actual).

---

# 🗣️ TTS: pipeline realista

1. Cuando usuario pulsa **Play**:

    * Si el PDF tiene texto cacheado por página → **cargar párrafos** de la página actual (y siguientes en buffer).
    * Si no hay texto y OCR está “on” → OCR de esa página (mostrar “extrayendo texto…” con progreso).
2. Dividir en **párrafos** (saltos de línea / heurística visual).
3. **Enviar párrafo** a `TextToSpeech.speak()` con `QUEUE_ADD`.
4. **Resaltar** el párrafo actual en el lector (overlay).
5. Al finalizar párrafo → siguiente; al final de página → página siguiente.
6. Permitir **saltar párrafo** y **saltar página** desde controles.
7. **MediaSession** para controles globales; **ForegroundService** para estabilidad en background.

---

# 🖼️ UI / Diseño

* **Home/Biblioteca**: grid de tarjetas con portada, título, progreso; barra de búsqueda; filtros; FAB “Importar”.
* **Detalle del documento**: portada grande, metadatos, botón “Leer” y “Escuchar”; lista de marcadores y notas.
* **Lector**: interfaz minimal con barras auto-ocultables; indicador de página “12/238”; botón marcador; acciones TTS.
* **Temas**: claro/oscuro + Dynamic Color; esquinas redondeadas XL; sombras suaves; tipografía legible (inter o roboto).

Ejemplos de textos:

* Botón TTS: “Escuchar”, “Velocidad”, “Voz”
* Temporizador: “Apagar en 15 · 30 · 45 min”
* Estado OCR: “Extrayendo texto (pág. 12)…”
* Snackbars claros y cortos.

---

# 🔐 Permisos

* **Ninguno clásico** de almacenamiento. Solo **SAF** (document tree + persistable URI).
* **Internet**: no obligatorio; opcional si hay verificación de updates o traducción.
* **Foreground service** para reproducción (Audio).

---

# 🚀 Rendimiento y límites

* Generar portadas y miniaturas **en cola** (WorkManager) y cachear en disco.
* Indexar texto **por página** y guardar resultado (Room/archivos).
* Para PDFs gigantes: precargar solo ±2 páginas alrededor.
* OCR es costoso: **opcional**, con aviso de batería/tiempo.

---

# ✅ Criterios de aceptación (MVP)

* Importar varios PDFs y mostrarlos en biblioteca con portada y progreso.
* Abrir lector, navegar páginas fluidamente, zoom correcto.
* Añadir/quitar marcador; guardar progreso al salir y restaurarlo al volver.
* TTS reproduce párrafos con controles (play/pause/next/prev), **resaltado visible**, y funciona con pantalla apagada (notificación + lockscreen).
* Ajustes de TTS (voz, velocidad, tono) persistentes.
* Sin crasheos con PDFs de +200 páginas; consumo de memoria controlado.
* Accesibilidad básica: **TalkBack**, tamaños de fuente UI, contrastes adecuados.

---

# 🧪 Pruebas

* Unit: parsers de párrafos, repositorios Room.
* Instrumentadas: navegación lector, restaurar progreso, notificación en background.
* Carga: abrir PDF de 1 000 páginas (mock) sin ANRs.
* Batería: 30 min de TTS con pantalla apagada sin detenerse por Doze.

---

# 🧱 Estructura de módulos (opcional)

* `app` (Compose, navegación)
* `domain` (use-cases, modelos puros)
* `data` (Room, repos, fuentes PDF/OCR/TTS)
* `pdf` (render + extracción texto)
* `tts` (servicio + MediaSession)

---

# 📦 Entregables que puede devolverte Claude

* Proyecto **Android Studio** con Gradle, Kotlin, Compose, Hilt, Room.
* Implementación del lector con PdfRenderer o AndroidPdfViewer.
* Servicio TTS con MediaSession + notificación.
* DAO/Repos + tests básicos.
* Pantallas: Onboarding, Biblioteca, Detalle, Lector, Ajustes TTS.
* Guía README de build y notas de rendimiento.

---

# 🏷️ Nombre e icono (ideas)

* **Voz & PDF**, **LectoPDF**, **SonarPDF**, **OírLibro**.
  Icono: documento + ondas sonoras, colores Material 3.

---

## 📤 Texto listo para pegar a Claude (prompt)

> **Objetivo**: Crea una app Android en Kotlin con Jetpack Compose y Material 3 que lea PDFs y ofrezca TTS por párrafos con resaltado. Incluye biblioteca con importación vía SAF, portadas cacheadas, progreso, marcadores, notas y búsqueda.
> **Arquitectura**: MVVM + Hilt + Room + DataStore + Paging.
> **PDF**: Render con PdfRenderer o AndroidPdfViewer. **Extracción de texto** con PDFBox-Android; OCR on-device con ML Kit como opción.
> **TTS**: Servicio foreground con MediaSession, notificación, temporizador de sueño, selección de voz/velocidad/tono, resaltado del párrafo actual en el lector.
> **Persistencia**: Modelos Room (Document, Bookmark, Highlight, Note, ReadingProgress, Collection, Tag, VoiceProfile).
> **Flujos**: Onboarding (elegir carpeta SAF), Biblioteca (grid/lista, filtros), Detalle, Lector (zoom, salto página, marcadores, TTS), Ajustes TTS.
> **Accesibilidad**: TalkBack, tamaños de fuente UI, contraste.
> **Rendimiento**: Cache de portadas, extracción de texto por página bajo demanda, precarga ±2 páginas.
> **Criterios de aceptación**: TTS con resaltado y controles de sistema, lectura en background estable, progreso persistente, sin crasheos con PDFs grandes.
> **Entregable**: Proyecto Android Studio compilable, con README y tests básicos.
> **Extras (si da tiempo)**: exportar TTS a archivo (`synthesizeToFile`), detección automática de idioma por página, playlist de PDFs, estadísticas.

---

