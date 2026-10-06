package com.kokorofy.music

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.random.Random

/**
 * Listen Together experimental (LAN / offline-friendly).
 * Host emite por multicast UDP el songId + position + playing.
 * Guest escucha y aplica seek/play en el MediaController local
 * (ambos deben tener la canción offline o en catálogo).
 *
 * No requiere internet; sí Wi‑Fi/LAN o hotspot local.
 */
object ListenTogether {
    private const val GROUP = "239.60.60.60"
    private const val PORT = 39666

    @Volatile var sessionCode: String = ""
        private set
    @Volatile var isHost: Boolean = false
        private set
    @Volatile var isGuest: Boolean = false
        private set

    private var job: Job? = null

    fun startHost(scope: CoroutineScope): String {
        stop()
        isHost = true
        isGuest = false
        sessionCode = Random.nextInt(1000, 9999).toString()
        return sessionCode
    }

    fun broadcastState(songId: String, positionMs: Long, playing: Boolean) {
        if (!isHost) return
        try {
            val json = JSONObject()
                .put("code", sessionCode)
                .put("songId", songId)
                .put("pos", positionMs)
                .put("playing", playing)
                .toString()
            val data = json.toByteArray()
            MulticastSocket().use { socket ->
                val group = InetAddress.getByName(GROUP)
                val packet = DatagramPacket(data, data.size, group, PORT)
                socket.send(packet)
            }
        } catch (_: Exception) {}
    }

    fun joinAsGuest(
        scope: CoroutineScope,
        code: String,
        onPacket: (songId: String, pos: Long, playing: Boolean) -> Unit
    ) {
        stop()
        isGuest = true
        isHost = false
        sessionCode = code
        job = scope.launch(Dispatchers.IO) {
            try {
                MulticastSocket(PORT).use { socket ->
                    val group = InetAddress.getByName(GROUP)
                    socket.joinGroup(group)
                    val buf = ByteArray(2048)
                    while (isActive) {
                        val packet = DatagramPacket(buf, buf.size)
                        socket.receive(packet)
                        val text = String(packet.data, 0, packet.length)
                        runCatching {
                            val o = JSONObject(text)
                            if (o.optString("code") != code) return@runCatching
                            onPacket(
                                o.getString("songId"),
                                o.getLong("pos"),
                                o.getBoolean("playing")
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        isHost = false
        isGuest = false
        sessionCode = ""
    }
}
