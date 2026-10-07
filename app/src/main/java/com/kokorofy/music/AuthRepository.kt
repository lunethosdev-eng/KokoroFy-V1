package com.kokorofy.music

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class AuthState(val email: String? = null, val accessToken: String? = null)

class AuthRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val prefs = context.getSharedPreferences("kokorofy_auth", Context.MODE_PRIVATE)
    private val json = "application/json".toMediaType()

    fun state(): AuthState = AuthState(
        email = prefs.getString("email", null),
        accessToken = prefs.getString("access_token", null)
    )

    suspend fun signUp(email: String, password: String): Result<AuthState> = request(
        endpoint = "/auth/v1/signup",
        payload = JSONObject().put("email", email).put("password", password),
        storeEmail = email
    )

    suspend fun signIn(email: String, password: String): Result<AuthState> = request(
        endpoint = "/auth/v1/token?grant_type=password",
        payload = JSONObject().put("email", email).put("password", password),
        storeEmail = email
    )

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val token = prefs.getString("access_token", null)
        if (!token.isNullOrBlank()) {
            runCatching {
                val request = Request.Builder()
                    .url(Config.SUPABASE_URL + "/auth/v1/logout")
                    .header("apikey", Config.SUPABASE_PUBLISHABLE_KEY)
                    .header("Authorization", "Bearer $token")
                    .post("".toRequestBody(json))
                    .build()
                client.newCall(request).execute().close()
            }
        }
        prefs.edit().clear().apply()
    }

    private suspend fun request(endpoint: String, payload: JSONObject, storeEmail: String): Result<AuthState> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(Config.SUPABASE_URL + endpoint)
                    .header("apikey", Config.SUPABASE_PUBLISHABLE_KEY)
                    .header("Content-Type", "application/json")
                    .post(payload.toString().toRequestBody(json))
                    .build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = runCatching { JSONObject(body).optString("msg") }
                            .getOrDefault("")
                            .ifBlank { runCatching { JSONObject(body).optString("message") }.getOrDefault("") }
                            .ifBlank { "Error de autenticación (${response.code})" }
                        error(message)
                    }
                    val obj = JSONObject(body)
                    val token = obj.optString("access_token").ifBlank { null }
                    prefs.edit()
                        .putString("email", storeEmail)
                        .apply {
                            if (token != null) putString("access_token", token)
                        }
                        .apply()
                    AuthState(storeEmail, token ?: prefs.getString("access_token", null))
                }
            }
        }
}
