package com.kokorofy.music

import android.content.ComponentName
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KokoroFyTheme {
                KokoroFyRoot()
            }
        }
    }
}

/* ───────────────── Theme + Liquid Glass ───────────────── */

private val GlassWhite = Color(0xE6FFFFFF)
private val GlassPurple = Color(0x33A78BFA)
private val Bg = Color(0xFFF4F2FA)
private val Purple = Color(0xFF746BE8)
private val TextDark = Color(0xFF18181C)

@Composable
fun KokoroFyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Bg,
            surface = Color.White,
            primary = Purple,
            onSurface = TextDark
        ),
        content = content
    )
}

/** Superficie Liquid Glass: blur + tinte + highlight specular. */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .clip(corner)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xF2FFFFFF),
                        Color(0xCCFFFFFF),
                        Color(0x99E8E4FF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 1200f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xAAFFFFFF),
                        Color(0x33FFFFFF),
                        Color(0x66C4B5FD)
                    )
                ),
                shape = corner
            )
    ) {
        // Highlight specular arriba
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x55FFFFFF), Color.Transparent)
                    )
                )
        )
        content()
    }
}

/* ───────────────── Root ───────────────── */

@Composable
fun KokoroFyRoot() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val repo = remember { CatalogRepository(context) }
    val scope = rememberCoroutineScope()

    val songs by db.songDao().observeSongs().collectAsState(emptyList())
    val playlists by db.playlistDao().observePlaylists().collectAsState(emptyList())

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var current by remember { mutableStateOf<Song?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var tab by remember { mutableIntStateOf(0) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var showLyricsFull by remember { mutableStateOf(false) }
    var lyricsText by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var gyroEnabled by remember { mutableStateOf(false) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    // Conectar MediaController SIN bloquear UI
    LaunchedEffect(Unit) {
        runCatching { repo.refresh() }
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            runCatching {
                val c = future.get()
                controller = c
                // Restaurar canción actual si el servicio ya tenía algo
                c.currentMediaItem?.mediaId?.let { id ->
                    scope.launch {
                        current = db.songDao().get(id) ?: current
                    }
                }
                isPlaying = c.isPlaying
                position = c.currentPosition.coerceAtLeast(0L)
                duration = c.duration.takeIf { it > 0 } ?: 0L
            }
        }, context.mainExecutor)
    }

    // Listener del player → mini player no se reinicia
    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                item?.mediaId?.let { id ->
                    scope.launch {
                        current = db.songDao().get(id) ?: current
                    }
                }
            }
            override fun onPlaybackStateChanged(state: Int) {
                duration = c.duration.takeIf { it > 0 } ?: duration
            }
        }
        c.addListener(listener)
        onDispose {
            c.removeListener(listener)
        }
    }

    // Poll posición suave
    LaunchedEffect(controller) {
        while (true) {
            controller?.let {
                position = it.currentPosition.coerceAtLeast(0L)
                val d = it.duration
                if (d > 0) duration = d
                isPlaying = it.isPlaying
            }
            delay(250)
        }
    }

    DisposableEffect(Unit) {
        onDispose { /* no release — el servicio vive */ }
    }

    // Giroscopio experimental
    DisposableEffect(gyroEnabled) {
        if (!gyroEnabled) {
            tiltX = 0f; tiltY = 0f
            return@DisposableEffect onDispose {}
        }
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type == Sensor.TYPE_ROTATION_VECTOR && e.values.size >= 3) {
                    tiltX = (e.values[1] * 18f).coerceIn(-18f, 18f)
                    tiltY = (e.values[0] * 18f).coerceIn(-18f, 18f)
                } else if (e.values.size >= 2) {
                    tiltX = (e.values[0] * 2f).coerceIn(-18f, 18f)
                    tiltY = (e.values[1] * 2f).coerceIn(-18f, 18f)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensor?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onDispose { sm.unregisterListener(listener) }
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val c = controller ?: return
        // Si ya es la misma canción, solo resume (NO reinicia)
        if (c.currentMediaItem?.mediaId == song.id) {
            if (!c.isPlaying) c.play()
            current = song
            showFullPlayer = true
            return
        }
        val items = queue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(s.audioUrl)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.coverUrl?.let(android.net.Uri::parse))
                        .build()
                )
                .build()
        }
        val start = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        c.setMediaItems(items, start, 0L)
        c.prepare()
        c.play()
        current = song
        showFullPlayer = true
        lyricsText = null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8F6FF), Color(0xFFEDE9FE), Bg)
                )
            )
    ) {
        // Fondo difuminado del cover (Liquid Glass reflection base)
        current?.coverUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(48.dp)
                    .graphicsLayer {
                        alpha = 0.22f
                        translationX = tiltX * 1.2f
                        translationY = tiltY * 1.2f
                        scaleX = 1.08f
                        scaleY = 1.08f
                    }
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    // Mini player
                    AnimatedVisibility(
                        visible = current != null && !showFullPlayer,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        current?.let { song ->
                            MiniPlayer(
                                song = song,
                                isPlaying = isPlaying,
                                position = position,
                                duration = duration,
                                onOpen = { showFullPlayer = true },
                                onPlayPause = {
                                    controller?.let {
                                        if (it.isPlaying) it.pause() else it.play()
                                    }
                                },
                                onNext = { controller?.seekToNextMediaItem() }
                            )
                        }
                    }
                    // Nav
                    LiquidGlass(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        corner = RoundedCornerShape(28.dp)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            val items = listOf(
                                Triple(0, "Inicio", Icons.Filled.Home),
                                Triple(1, "Buscar", Icons.Filled.Search),
                                Triple(2, "Biblioteca", Icons.Filled.LibraryMusic),
                                Triple(3, "Ajustes", Icons.Filled.Settings)
                            )
                            items.forEach { (i, name, icon) ->
                                NavigationBarItem(
                                    selected = tab == i,
                                    onClick = {
                                        tab = i
                                        selectedPlaylist = null
                                    },
                                    icon = { Icon(icon, name) },
                                    label = { Text(name, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0x33746BE8)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad)) {
                when (tab) {
                    0 -> HomeScreen(
                        songs = songs,
                        onPlay = { playSong(it, songs) },
                        onDownload = { OfflineManager.download(context, it) }
                    )
                    1 -> SearchScreen(
                        songs = songs,
                        query = query,
                        onQuery = { query = it },
                        onPlay = { playSong(it, songs) },
                        onDownload = { OfflineManager.download(context, it) }
                    )
                    2 -> LibraryScreen(
                        songs = songs,
                        playlists = playlists,
                        selected = selectedPlaylist,
                        onSelectPlaylist = { selectedPlaylist = it },
                        onCreatePlaylist = { name ->
                            scope.launch {
                                db.playlistDao().upsert(
                                    Playlist(UUID.randomUUID().toString(), name)
                                )
                            }
                        },
                        onPlay = { playSong(it, songs) },
                        onAddToPlaylist = { pl, song ->
                            scope.launch {
                                db.playlistDao().addSong(
                                    PlaylistSong(pl.id, song.id)
                                )
                            }
                        },
                        onDownload = { OfflineManager.download(context, it) },
                        db = db
                    )
                    3 -> SettingsScreen(
                        gyroEnabled = gyroEnabled,
                        onGyro = { gyroEnabled = it },
                        songCount = songs.size
                    )
                }
            }
        }

        // Full Player con slide-down
        AnimatedVisibility(
            visible = showFullPlayer && current != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(380, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(280)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(220)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            current?.let { song ->
                FullPlayer(
                    song = song,
                    controller = controller,
                    isPlaying = isPlaying,
                    position = position,
                    duration = duration,
                    lyricsText = lyricsText,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    gyroEnabled = gyroEnabled,
                    onClose = { showFullPlayer = false },
                    onLoadLyrics = {
                        scope.launch {
                            val r = LyricsRepository().fetch(song)
                            lyricsText = r?.synced ?: r?.plain
                        }
                    },
                    onLyricsFull = { showLyricsFull = true },
                    onDownload = { OfflineManager.download(context, song) },
                    onSeek = { controller?.seekTo(it) },
                    onPrev = { controller?.seekToPreviousMediaItem() },
                    onNext = { controller?.seekToNextMediaItem() },
                    onShuffle = {
                        controller?.shuffleModeEnabled =
                            !(controller?.shuffleModeEnabled ?: false)
                    },
                    onRepeat = {
                        val mode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
                        controller?.repeatMode = when (mode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                            else -> Player.REPEAT_MODE_OFF
                        }
                    },
                    onPlayPause = {
                        controller?.let {
                            if (it.isPlaying) it.pause() else it.play()
                        }
                    }
                )
            }
        }

        // Lyrics fullscreen karaoke
        AnimatedVisibility(
            visible = showLyricsFull && current != null,
            enter = fadeIn() + scaleIn(initialScale = 0.96f),
            exit = fadeOut() + scaleOut(targetScale = 0.96f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(20f)
        ) {
            current?.let { song ->
                LyricsFullscreen(
                    song = song,
                    lyricsText = lyricsText,
                    position = position,
                    onClose = { showLyricsFull = false }
                )
            }
        }
    }
}

/* ───────────────── Screens ───────────────── */

@Composable
fun HomeScreen(
    songs: List<Song>,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = R.drawable.kokorofy_icon,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(10.dp))
            Text("KokoroFy", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Spacer(Modifier.height(22.dp))
        Text("PARA TI", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(
            "Escucha lo que te gusta.",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.8).sp
        )
        Spacer(Modifier.height(16.dp))
        if (songs.isEmpty()) {
            Text("Cargando catálogo…", color = Color.Gray)
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                TrackRow(song, onPlay = { onPlay(song) }, onDownload = { onDownload(song) })
            }
        }
    }
}

@Composable
fun SearchScreen(
    songs: List<Song>,
    query: String,
    onQuery: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    val filtered = remember(songs, query) {
        if (query.isBlank()) emptyList()
        else songs.filter {
            "${it.title} ${it.artist} ${it.album}".contains(query, ignoreCase = true)
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Buscar", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(18.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                singleLine = true,
                placeholder = { Text("Artistas, canciones, álbumes") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
        Spacer(Modifier.height(16.dp))
        if (query.isBlank()) {
            Text(
                "Escribe para buscar en tu catálogo",
                color = Color.Gray,
                modifier = Modifier.padding(top = 40.dp)
            )
        } else if (filtered.isEmpty()) {
            Text("Sin resultados", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { song ->
                    TrackRow(song, onPlay = { onPlay(song) }, onDownload = { onDownload(song) })
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    selected: Playlist?,
    onSelectPlaylist: (Playlist?) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onAddToPlaylist: (Playlist, Song) -> Unit,
    onDownload: (Song) -> Unit,
    db: AppDatabase
) {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val playlistSongs by if (selected != null) {
        db.playlistDao().observePlaylistSongs(selected.id).collectAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected != null) {
                IconButton(onClick = { onSelectPlaylist(null) }) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
            }
            Text(
                selected?.name ?: "Biblioteca",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            if (selected == null) {
                IconButton(onClick = { showCreate = true }) {
                    Icon(Icons.Default.PlaylistAdd, "Nueva playlist")
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (selected == null) {
            Text("PLAYLISTS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (playlists.isEmpty()) {
                Text("Crea tu primera playlist", color = Color.Gray, fontSize = 14.sp)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(playlists, key = { it.id }) { pl ->
                    LiquidGlass(corner = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPlaylist(pl) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QueueMusic, null, tint = Purple)
                            Spacer(Modifier.width(12.dp))
                            Text(pl.name, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("TODAS LAS CANCIONES", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
                items(songs, key = { "all-${it.id}" }) { song ->
                    TrackRow(song, onPlay = { onPlay(song) }, onDownload = { onDownload(song) })
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (playlistSongs.isEmpty()) {
                    item {
                        Text(
                            "Playlist vacía. Agrega canciones desde Inicio (mantén el icono de descarga / menú).",
                            color = Color.Gray
                        )
                    }
                }
                items(playlistSongs, key = { it.id }) { song ->
                    TrackRow(song, onPlay = { onPlay(song) }, onDownload = { onDownload(song) })
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Nueva playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    label = { Text("Nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        onCreatePlaylist(newName.trim())
                        newName = ""
                        showCreate = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    gyroEnabled: Boolean,
    onGyro: (Boolean) -> Unit,
    songCount: Int
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text("Ajustes", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("EXPERIMENTAL", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Efecto 3D cinemático", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Usa el giroscopio: al inclinar el teléfono el fondo se difumina con parallax suave.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(checked = gyroEnabled, onCheckedChange = onGyro)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("CATÁLOGO", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("$songCount canciones en caché local", fontSize = 14.sp)
                Text(
                    "Las descargas offline se guardan en la caché interna de la app (Media3), no en la galería.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("KokoroFy", fontWeight = FontWeight.Bold)
                Text("v1.1 · Liquid Glass · Native Player", fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

/* ───────────────── Rows / Mini / Full ───────────────── */

@Composable
fun TrackRow(song: Song, onPlay: () -> Unit, onDownload: () -> Unit) {
    LiquidGlass(corner = RoundedCornerShape(16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onPlay)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl ?: R.drawable.kokorofy_icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(song.artist, color = Color.Gray, fontSize = 13.sp, maxLines = 1)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Outlined.DownloadForOffline, "Descargar offline")
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    val progress = if (duration > 0) position.toFloat() / duration else 0f
    LiquidGlass(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onOpen),
        corner = RoundedCornerShape(18.dp)
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = Purple,
                trackColor = Color(0x22000000)
            )
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.coverUrl ?: R.drawable.kokorofy_icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        song.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(song.artist, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause"
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.SkipNext, "Siguiente")
                }
            }
        }
    }
}

@Composable
fun FullPlayer(
    song: Song,
    controller: MediaController?,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    lyricsText: String?,
    tiltX: Float,
    tiltY: Float,
    gyroEnabled: Boolean,
    onClose: () -> Unit,
    onLoadLyrics: () -> Unit,
    onLyricsFull: () -> Unit,
    onDownload: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onPlayPause: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // Fondo cover blur
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
                .graphicsLayer {
                    alpha = 0.55f
                    if (gyroEnabled) {
                        translationX = tiltX * 2f
                        translationY = tiltY * 2f
                        scaleX = 1.12f
                        scaleY = 1.12f
                    }
                }
        )
        // Overlay oscuro suave
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, "Cerrar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "REPRODUCIENDO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xCCFFFFFF)
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDownload) {
                    Icon(Icons.Outlined.DownloadForOffline, "Offline", tint = Color.White)
                }
            }

            Spacer(Modifier.height(18.dp))
            AsyncImage(
                model = song.coverUrl ?: R.drawable.kokorofy_icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .graphicsLayer {
                        if (gyroEnabled) {
                            rotationX = tiltY * 0.35f
                            rotationY = -tiltX * 0.35f
                            cameraDistance = 12f * density
                        }
                    }
            )
            Spacer(Modifier.height(22.dp))
            Text(song.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(song.artist, fontSize = 16.sp, color = Color(0xCCFFFFFF))
            Spacer(Modifier.height(12.dp))

            Slider(
                value = if (duration > 0) position.toFloat() / duration else 0f,
                onValueChange = { v -> onSeek((v * duration).toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color(0x44FFFFFF)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position), fontSize = 12.sp, color = Color(0xAAFFFFFF))
                Text(formatTime(duration), fontSize = 12.sp, color = Color(0xAAFFFFFF))
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(
                        Icons.Default.Shuffle, "Shuffle",
                        tint = if (controller?.shuffleModeEnabled == true) Purple else Color.White
                    )
                }
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(68.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Play",
                        tint = TextDark,
                        modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = onRepeat) {
                    val mode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
                    Icon(
                        when (mode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            Player.REPEAT_MODE_ALL -> Icons.Default.Repeat
                            else -> Icons.Default.Repeat
                        },
                        "Repeat",
                        tint = if (mode != Player.REPEAT_MODE_OFF) Purple else Color.White
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            LiquidGlass(corner = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("LETRAS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            if (lyricsText == null) onLoadLyrics()
                            onLyricsFull()
                        }) {
                            Text("Fullscreen")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (lyricsText == null) {
                        TextButton(onClick = onLoadLyrics) {
                            Text("Cargar letras")
                        }
                    } else {
                        KaraokePreview(lyricsText!!, position)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun KaraokePreview(lyrics: String, position: Long) {
    val lines = remember(lyrics) { parseLrc(lyrics) }
    if (lines.isEmpty()) {
        Text(lyrics, fontSize = 16.sp, lineHeight = 26.sp, maxLines = 8, overflow = TextOverflow.Ellipsis)
        return
    }
    val active = lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    Column {
        lines.drop(maxOf(0, active - 1)).take(5).forEachIndexed { i, line ->
            val isActive = lines.indexOf(line) == active
            Text(
                line.text,
                fontSize = if (isActive) 18.sp else 15.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Purple else Color.Gray,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }
}

@Composable
fun LyricsFullscreen(
    song: Song,
    lyricsText: String?,
    position: Long,
    onClose: () -> Unit
) {
    val lines = remember(lyricsText) { parseLrc(lyricsText ?: "") }
    val listState = rememberLazyListState()
    val active = if (lines.isEmpty()) -1 else lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)

    LaunchedEffect(active) {
        if (active >= 0) {
            runCatching { listState.animateScrollToItem(maxOf(0, active - 2)) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
                .graphicsLayer { alpha = 0.45f }
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xBB000000))
        )
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(song.artist, color = Color(0xAAFFFFFF), fontSize = 13.sp)
                }
            }
            if (lyricsText.isNullOrBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin letras", color = Color.White)
                }
            } else if (lines.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(lyricsText, color = Color.White, fontSize = 22.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(lines.size) { i ->
                        val line = lines[i]
                        val isActive = i == active
                        Text(
                            line.text,
                            fontSize = if (isActive) 28.sp else 18.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) Color.White else Color(0x66FFFFFF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

/* ───────────────── Helpers ───────────────── */

fun formatTime(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}

fun parseLrc(raw: String): List<LyricLine> {
    if (raw.isBlank()) return emptyList()
    val regex = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]\s*(.*)""")
    val out = mutableListOf<LyricLine>()
    raw.lineSequence().forEach { line ->
        val m = regex.find(line.trim())
        if (m != null) {
            val min = m.groupValues[1].toLongOrNull() ?: return@forEach
            val sec = m.groupValues[2].toLongOrNull() ?: return@forEach
            val frac = m.groupValues[3].let {
                when {
                    it.isEmpty() -> 0L
                    it.length == 1 -> it.toLong() * 100
                    it.length == 2 -> it.toLong() * 10
                    else -> it.take(3).toLong()
                }
            }
            val text = m.groupValues[4].trim()
            if (text.isNotEmpty()) {
                out += LyricLine(min * 60_000 + sec * 1000 + frac, text)
            }
        }
    }
    return out.sortedBy { it.timeMs }
}
