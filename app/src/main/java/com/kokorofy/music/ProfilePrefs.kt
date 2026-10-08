package com.kokorofy.music

import android.content.Context
import android.net.Uri

/** Local profile: display name, username, avatar URI. */
object ProfilePrefs {
    private const val N = "kokorofy_profile_v1"
    private fun sp(c: Context) = c.getSharedPreferences(N, Context.MODE_PRIVATE)

    fun displayName(c: Context) = sp(c).getString("name", "") ?: ""
    fun setDisplayName(c: Context, v: String) = sp(c).edit().putString("name", v.trim()).apply()

    fun username(c: Context) = sp(c).getString("username", "") ?: ""
    fun setUsername(c: Context, v: String) = sp(c).edit().putString("username", v.trim().removePrefix("@")).apply()

    fun avatarUri(c: Context): String? = sp(c).getString("avatar", null)?.takeIf { it.isNotBlank() }
    fun setAvatarUri(c: Context, uri: String?) = sp(c).edit().putString("avatar", uri).apply()

    fun hasProfile(c: Context) = displayName(c).isNotBlank() || username(c).isNotBlank() || avatarUri(c) != null
}
