package com.kokorofy.music

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KokoroFyTheme { KokoroFyApp() } }
    }
}

@Composable
fun KokoroFyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Color(0xFFF8F8FB),
            surface = Color.White,
            primary = Color(0xFF746BE8),
            onSurface = Color(0xFF18181C)
        ),
        content = content
    )
}

@Composable
fun KokoroFyApp() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val repo = remember { CatalogRepository(context) }
    val scope = rememberCoroutineScope()
    val songs by db.songDao().observeSongs().collectAsState(emptyList())
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var current by remember { mutableStateOf<Song?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) }
    var lyrics by remember { mutableStateOf<String?>(null) }
    var showEq by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Cargar catálogo sin bloquear la UI
        runCatching { repo.refresh() }

        // Conectar MediaController de forma asíncrona (no usa .get())
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                runCatching { controller = future.get() }
            },
            context.mainExecutor
        )
    }

    DisposableEffect(Unit) {
        onDispose { controller?.release() }
    }

    val filtered = songs.filter {
        "${it.title} ${it.artist} ${it.album}".contains(query, true)
    }

    fun play(song: Song) {
        current = song
        showPlayer = true
        controller?.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(song.id)
                    .setUri(song.audioUrl)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(song.coverUrl?.let(android.net.Uri::parse))
                            .build()
                    ).build()
            )
            prepare()
            play()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("Inicio", "Buscar", "Biblioteca").forEachIndexed { i, name ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = {
                            Icon(
                                if (i == 0) Icons.Default.Home
                                else if (i == 1) Icons.Default.Search
                                else Icons.Default.LibraryMusic,
                                name
                            )
                        },
                        label = { Text(name) }
                    )
                }
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = R.drawable.kokorofy_icon,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text("KokoroFy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showEq = !showEq }) {
                    Icon(Icons.Default.Equalizer, "Ecualizador")
                }
            }

            if (tab == 1) {
                OutlinedTextField(
                    query, { query = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                    singleLine = true,
                    label = { Text("Buscar música") }
                )
            }

            if (tab == 0) {
                Spacer(Modifier.height(25.dp))
                Text("PARA TI", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    "Escucha lo que te gusta.",
                    fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp
                )
                Spacer(Modifier.height(18.dp))
            } else {
                Spacer(Modifier.height(18.dp))
                Text(if (tab == 1) "Resultados" else "Tu biblioteca", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
            }

            if (showEq) EqualizerPanel()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.id }) { song ->
                    TrackRow(song, onPlay = { play(song) }, onDownload = { OfflineManager.download(context, song) })
                }
            }
        }

        if (showPlayer && current != null) {
            FullPlayer(
                song = current!!,
                controller = controller,
                onClose = { showPlayer = false },
                onLyrics = {
                    scope.launch {
                        val result = LyricsRepository().fetch(current!!)
                        lyrics = result?.synced ?: result?.plain
                    }
                },
                onDownload = { OfflineManager.download(context, current!!) },
                lyrics = lyrics
            )
        }
    }
}

@Composable
fun TrackRow(song: Song, onPlay: () -> Unit, onDownload: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onPlay).padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(62.dp).clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.SemiBold)
            Text(song.artist, color = Color.Gray, fontSize = 13.sp)
        }
        IconButton(onClick = onDownload) {
            Icon(Icons.Default.Download, "Disponible sin conexión")
        }
    }
}

@Composable
fun EqualizerPanel() {
    val transition = rememberInfiniteTransition(label = "eq")
    Row(
        Modifier.fillMaxWidth().height(85.dp).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(16) { i ->
            val h by transition.animateFloat(
                initialValue = 10f + (i % 5) * 5f,
                targetValue = 65f - (i % 4) * 7f,
                animationSpec = infiniteRepeatable(
                    tween(300 + i * 35, easing = LinearEasing),
                    RepeatMode.Reverse
                ),
                label = "bar$i"
            )
            Box(
                Modifier.width(5.dp).height(h.dp).clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFB8B2FF), Color(0xFF6D64DD))
                        )
                    )
            )
        }
    }
}

@Composable
fun FullPlayer(
    song: Song,
    controller: MediaController?,
    onClose: () -> Unit,
    onLyrics: () -> Unit,
    onDownload: () -> Unit,
    lyrics: String?
) {
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(song.duration) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller, song.id) {
        while (true) {
            controller?.let {
                position = it.currentPosition.coerceAtLeast(0L)
                duration = it.duration.takeIf { d -> d > 0 } ?: duration
            }
            kotlinx.coroutines.delay(250)
        }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF8F8FB))
            .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, "Cerrar") }
            Spacer(Modifier.weight(1f))
            Text("REPRODUCIENDO AHORA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDownload) { Icon(Icons.Default.Download, "Descargar") }
        }

        Spacer(Modifier.height(22.dp))
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp))
        )
        Spacer(Modifier.height(20.dp))
        Text(song.title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(song.artist, fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(15.dp))

        Slider(
            value = if (duration > 0) position.toFloat() / duration else 0f,
            onValueChange = { v ->
                controller?.seekTo((v * duration).toLong())
            }
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(position), fontSize = 11.sp)
            Text(formatTime(duration), fontSize = 11.sp)
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false) }) {
                Icon(Icons.Default.Shuffle, "Aleatorio")
            }
            IconButton(onClick = { controller?.seekToPreviousMediaItem() }) {
                Icon(Icons.Default.SkipPrevious, "Anterior")
            }
            FilledIconButton(
                onClick = {
                    if (controller?.isPlaying == true) controller.pause() else controller?.play()
                },
                modifier = Modifier.size(66.dp),
                shape = CircleShape
            ) {
                Icon(
                    if (controller?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                    "Reproducir"
                )
            }
            IconButton(onClick = { controller?.seekToNextMediaItem() }) {
                Icon(Icons.Default.SkipNext, "Siguiente")
            }
            IconButton(onClick = onLyrics) { Icon(Icons.Default.Lyrics, "Letras") }
        }

        if (lyrics != null) {
            Spacer(Modifier.height(15.dp))
            Text("LETRAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(
                lyrics,
                fontSize = 18.sp,
                lineHeight = 29.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}

fun formatTime(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}
