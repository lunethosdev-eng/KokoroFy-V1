package com.kokorofy.music

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Descarga offline con progreso real (0–100).
 * UI debe leer [progress] — sin gradientes, solo % y título.
 */
object OfflineManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    data class Progress(
        val active: Boolean = false,
        val title: String = "",
        val percent: Int = 0,
        val done: Boolean = false,
        val error: String? = null
    )

    private val _progress = MutableStateFlow(Progress())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    fun localFile(context: Context, songId: String): File {
        val dir = File(context.filesDir, "offline").also { if (!it.exists()) it.mkdirs() }
        return File(dir, "$songId.mp3")
    }

    fun isOffline(context: Context, songId: String): Boolean =
        localFile(context, songId).exists()

    fun offlineCount(context: Context): Int {
        val dir = File(context.filesDir, "offline")
        return dir.listFiles()?.count { it.isFile && it.extension == "mp3" } ?: 0
    }

    fun playUri(context: Context, song: Song): String {
        val f = localFile(context, song.id)
        return if (f.exists()) f.toURI().toString() else song.audioUrl
    }

    fun download(context: Context, song: Song) {
        if (song.audioUrl.isBlank()) {
            Toast.makeText(context, "Sin URL de audio", Toast.LENGTH_SHORT).show()
            return
        }
        if (isOffline(context, song.id)) {
            Toast.makeText(context, "Ya está offline", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            _progress.value = Progress(active = true, title = song.title, percent = 0)
            val ok = runCatching {
                val req = Request.Builder().url(song.audioUrl).build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) error("HTTP ${resp.code}")
                    val body = resp.body ?: error("empty")
                    val total = body.contentLength()
                    val dest = localFile(context, song.id)
                    val tmp = File(dest.parent, "${dest.name}.part")
                    body.byteStream().use { input ->
                        tmp.outputStream().use { output ->
                            val buf = ByteArray(16 * 1024)
                            var read = 0L
                            while (true) {
                                val n = input.read(buf)
                                if (n <= 0) break
                                output.write(buf, 0, n)
                                read += n
                                val pct = if (total > 0) ((read * 100) / total).toInt().coerceIn(0, 99)
                                else ((read / (256 * 1024)).toInt() % 90)
                                _progress.value = Progress(true, song.title, pct)
                            }
                        }
                    }
                    if (!tmp.renameTo(dest)) {
                        tmp.copyTo(dest, overwrite = true)
                        tmp.delete()
                    }
                }
                true
            }.getOrElse {
                it.printStackTrace()
                _progress.value = Progress(active = true, title = song.title, percent = 0, error = it.message)
                false
            }
            _progress.value = Progress(
                active = true,
                title = song.title,
                percent = if (ok) 100 else _progress.value.percent,
                done = true,
                error = if (ok) null else (_progress.value.error ?: "Error")
            )
            kotlinx.coroutines.delay(900)
            _progress.value = Progress()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    if (ok) "«${song.title}» offline" else "Error al descargar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun remove(context: Context, song: Song) {
        localFile(context, song.id).delete()
    }
}
