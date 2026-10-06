package com.kokorofy.music

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class AppUpdate(
    val tag: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val apkUrl: String?
)

/**
 * Consulta GitHub Releases del repo KokoroFy-V1.
 * Si hay una versión más nueva → popup en la app.
 * Instalar APK desde releases = actualizar sin borrar datos (mismo applicationId).
 */
object UpdateChecker {
    private const val API =
        "https://api.github.com/repos/lunethosdev-eng/KokoroFy-V1/releases/latest"
    private val client = OkHttpClient()

    suspend fun check(currentVersionName: String): AppUpdate? = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder()
                .url(API)
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val o = JSONObject(resp.body?.string() ?: return@use null)
                val tag = o.optString("tag_name").removePrefix("v")
                if (tag.isBlank() || tag == currentVersionName) return@use null
                // simple compare: if different, offer update
                if (!isNewer(tag, currentVersionName)) return@use null
                val assets = o.optJSONArray("assets")
                var apk: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val a = assets.getJSONObject(i)
                        val name = a.optString("name")
                        if (name.endsWith(".apk", true)) {
                            apk = a.optString("browser_download_url")
                            break
                        }
                    }
                }
                AppUpdate(
                    tag = tag,
                    name = o.optString("name", "v$tag"),
                    body = o.optString("body", ""),
                    htmlUrl = o.optString("html_url"),
                    apkUrl = apk
                )
            }
        }.getOrNull()
    }

    private fun isNewer(remote: String, local: String): Boolean {
        fun parts(v: String) = v.split(".", "-").mapNotNull { it.toIntOrNull() }
        val r = parts(remote)
        val l = parts(local)
        val n = maxOf(r.size, l.size)
        for (i in 0 until n) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a > b) return true
            if (a < b) return false
        }
        return false
    }

    fun openUrl(context: Context, url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
