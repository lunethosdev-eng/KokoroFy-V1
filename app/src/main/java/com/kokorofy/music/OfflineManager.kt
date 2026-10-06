package com.kokorofy.music

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Descargas offline DENTRO de la app (filesDir/offline/).
 * No usa DownloadService de Media3 → no crashea.
 * Al reproducir, si existe archivo local se usa ese URI.
 */
object OfflineManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private fun dir(context: Context): File =
        File(context.filesDir, "offline").also { if (!it.exists()) it.mkdirs() }

    fun localFile(context: Context, songId: String): File =
        File(dir(context), "$songId.mp3")

    fun isDownloaded(context: Context, songId: String): Boolean =
        localFile(context, songId).let { it.exists() && it.length() > 1024 }

    /** URI local offline, content:// del dispositivo, o URL remota. */
    fun playUri(context: Context, song: Song): String {
        val f = localFile(context, song.id)
        if (f.exists() && f.length() > 1024) return f.toURI().toString()
        // content:// o file:// del escáner local → se usan tal cual
        if (song.audioUrl.startsWith("content://") || song.audioUrl.startsWith("file://")) {
            return song.audioUrl
        }
        // Ruta absoluta legada → file://
        if (song.audioUrl.startsWith("/")) {
            return java.io.File(song.audioUrl).toURI().toString()
        }
        return song.audioUrl
    }

    fun download(context: Context, song: Song) {
        if (song.audioUrl.isBlank()) {
            Toast.makeText(context, "Sin URL de audio", Toast.LENGTH_SHORT).show()
            return
        }
        if (isDownloaded(context, song.id)) {
            Toast.makeText(context, "Ya está descargada", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "Descargando «${song.title}»…", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            val ok = runCatching {
                val req = Request.Builder().url(song.audioUrl).build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) error("HTTP ${resp.code}")
                    val body = resp.body ?: error("empty body")
                    val dest = localFile(context, song.id)
                    val tmp = File(dest.parent, "${dest.name}.part")
                    body.byteStream().use { input ->
                        tmp.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (!tmp.renameTo(dest)) {
                        tmp.copyTo(dest, overwrite = true)
                        tmp.delete()
                    }
                }
                true
            }.getOrElse {
                it.printStackTrace()
                false
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    if (ok) "«${song.title}» lista offline ✓" else "Error al descargar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun remove(context: Context, song: Song) {
        localFile(context, song.id).delete()
        Toast.makeText(context, "Eliminada de offline", Toast.LENGTH_SHORT).show()
    }

    fun offlineCount(context: Context): Int =
        dir(context).listFiles()?.count { it.extension == "mp3" && it.length() > 1024 } ?: 0
}
