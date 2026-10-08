package com.kokorofy.music

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Seki API (Render).
 *
 * The app sends the secret in x-api-key first. If the backend rejects that
 * request with 401/403, SekiClient retries with ?key= for older deployments.
 */
interface SekiApi {
    @GET("api/search")
    suspend fun search(
        @Query("q") query: String,
        @Header("x-api-key") apiKey: String? = null,
        @Query("key") queryKey: String? = null
    ): Response<ResponseBody>
}
