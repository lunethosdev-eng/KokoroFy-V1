package com.kokorofy.music

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KokoroFyTheme { KokoroFyRoot() } }
    }
}

private val Bg = Color(0xFFF4F2FA)
private val Purple = Color(0xFF746BE8)
private val TextDark = Color(0xFF18181C)

@Composable
fun KokoroFyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Bg, surface = Color.White, primary = Purple, onSurface = TextDark
        ),
        content = content
    )
}

@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val ctx = LocalContext.current
    val intensity = Prefs.glassIntensity(ctx).coerceIn(0.4f, 1f)
    Box(
        modifier
            .clip(corner)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.92f * intensity),
                        Color.White.copy(alpha = 0.72f * intensity),
                        Color(0xFFE8E4FF).copy(alpha = 0.55f * intensity)
                    ),
                    start = Offset.Zero, end = Offset(900f, 1400f)
                )
            )
            .border(
                width = 1.1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.15f),
                        Color(0xFFC4B5FD).copy(alpha = 0.45f)
                    )
                ),
                shape = corner
            )
    ) {
        // Specular highlight (Apple-style)
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        content()
    }
}

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
    var showAddToPlaylist by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdate?>(null) }
    var accentColor by remember { mutableStateOf(Purple) }
    var reduceMotion by remember { mutableStateOf(false) }

    // MediaController async
    // Cargar prefs de UI
    LaunchedEffect(Unit) {
        gyroEnabled = Prefs.gyro(context)
        reduceMotion = Prefs.reduceMotion(context)
        val accents = listOf(
            Color(0xFF746BE8), Color(0xFFEC4899), Color(0xFF06B6D4),
            Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)
        )
        accentColor = accents.getOrElse(Prefs.accentIndex(context)) { accents[0] }
    }

    // Catálogo auto-refresh (sin borrar datos) cada 3 min + al abrir
    LaunchedEffect(Unit) {
        while (true) {
            runCatching {
                val n = repo.refresh()
                android.util.Log.i("KokoroFy", "Catálogo actualizado: $n")
            }
            kotlinx.coroutines.delay(3 * 60 * 1000L)
        }
    }

    LaunchedEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            runCatching {
                val c = future.get()
                controller = c
                // Velocidad de reproducción real
                c.setPlaybackSpeed(Prefs.playbackSpeed(context))
                c.currentMediaItem?.mediaId?.let { id ->
                    scope.launch { current = db.songDao().get(id) ?: current }
                }
                isPlaying = c.isPlaying
                position = c.currentPosition.coerceAtLeast(0L)
                duration = c.duration.takeIf { it > 0 } ?: 0L
            }
        }, context.mainExecutor)

        UpdateChecker.check(BuildConfigVersion.NAME)?.let { updateInfo = it }
    }

    // Widget sync
    LaunchedEffect(current?.id, isPlaying) {
        current?.let {
            PlayerWidget.sync(context, it.title, it.artist, isPlaying)
        }
    }

    // Changelog una sola vez por versión
    var showChangelog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (Changelog.shouldShow(context)) showChangelog = true
    }


    // Cuando cambia la canción actual → recargar lyrics de esa canción
    LaunchedEffect(current?.id) {
        lyricsText = null
        val song = current ?: return@LaunchedEffect
        if (!song.lyrics.isNullOrBlank()) {
            lyricsText = song.lyrics
        }
    }

    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                item?.mediaId?.let { id ->
                    scope.launch {
                        current = db.songDao().get(id) ?: current
                        lyricsText = null // no quedar atrapado en lyrics anteriores
                    }
                }
            }
            override fun onPlaybackStateChanged(state: Int) {
                duration = c.duration.takeIf { it > 0 } ?: duration
            }
        }
        c.addListener(listener)
        onDispose { c.removeListener(listener) }
    }

    LaunchedEffect(controller) {
        while (true) {
            controller?.let {
                position = it.currentPosition.coerceAtLeast(0L)
                val d = it.duration
                if (d > 0) duration = d
                isPlaying = it.isPlaying
            }
            kotlinx.coroutines.delay(250)
        }
    }

    // Gyro smoothed
    DisposableEffect(gyroEnabled) {
        if (!gyroEnabled) {
            tiltX = 0f; tiltY = 0f
            return@DisposableEffect onDispose {}
        }
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val listener = object : SensorEventListener {
            private var sx = 0f
            private var sy = 0f
            override fun onSensorChanged(e: SensorEvent) {
                val rawX: Float
                val rawY: Float
                when (e.sensor.type) {
                    Sensor.TYPE_GRAVITY -> {
                        rawX = e.values[0] * 3f
                        rawY = e.values[1] * 3f
                    }
                    else -> {
                        rawX = (e.values.getOrNull(1) ?: 0f) * 25f
                        rawY = (e.values.getOrNull(0) ?: 0f) * 25f
                    }
                }
                sx = sx * 0.85f + rawX * 0.15f
                sy = sy * 0.85f + rawY * 0.15f
                tiltX = sx.coerceIn(-22f, 22f)
                tiltY = sy.coerceIn(-22f, 22f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensor?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onDispose { sm.unregisterListener(listener) }
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val c = controller ?: return
        if (c.currentMediaItem?.mediaId == song.id) {
            if (!c.isPlaying) c.play()
            current = song
            showFullPlayer = true
            return
        }
        val items = queue.map { s ->
            val uri = OfflineManager.playUri(context, s)
            MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(uri)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.coverUrl?.let(android.net.Uri::parse))
                        .build()
                ).build()
        }
        val start = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        c.setMediaItems(items, start, 0L)
        c.prepare()
        c.play()
        current = song
        showFullPlayer = true
        lyricsText = null
    }

    // Permission for local music
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val local = LocalMusicScanner.scanDevice(context)
                if (local.isNotEmpty()) {
                    db.songDao().upsertAll(local)
                    Toast.makeText(context, "${local.size} canciones locales", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No se encontró música", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = BackupManager.exportAll(context, songs, playlists)
                BackupManager.writeToUri(context, uri, json)
                Toast.makeText(context, "Exportado ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = BackupManager.readFromUri(context, uri)
                val n = BackupManager.importJson(context, json)
                Toast.makeText(context, "Importadas $n canciones", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF8F6FF), Bg)))) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = current != null && !showFullPlayer && !showLyricsFull,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        current?.let { song ->
                            MiniPlayer(
                                song = song,
                                isPlaying = isPlaying,
                                position = position,
                                duration = duration,
                                accent = accentColor,
                                onOpen = { showFullPlayer = true },
                                onPlayPause = {
                                    controller?.let { if (it.isPlaying) it.pause() else it.play() }
                                },
                                onNext = { controller?.seekToNextMediaItem() }
                            )
                        }
                    }
                    LiquidGlass(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        corner = RoundedCornerShape(28.dp)
                    ) {
                        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                            listOf(
                                Triple(0, "Inicio", Icons.Filled.Home),
                                Triple(1, "Buscar", Icons.Filled.Search),
                                Triple(2, "Biblioteca", Icons.Filled.LibraryMusic),
                                Triple(3, "Ajustes", Icons.Filled.Settings)
                            ).forEach { (i, name, icon) ->
                                NavigationBarItem(
                                    selected = tab == i,
                                    onClick = { tab = i; selectedPlaylist = null },
                                    icon = { Icon(icon, name) },
                                    label = { Text(name, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(indicatorColor = accentColor.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad)) {
                when (tab) {
                    0 -> HomeScreen(songs, accentColor, { playSong(it, songs) }) {
                        OfflineManager.download(context, it)
                    }
                    1 -> SearchScreen(songs, query, { query = it }, { playSong(it, songs) }) {
                        OfflineManager.download(context, it)
                    }
                    2 -> LibraryScreen(
                        songs, playlists, selectedPlaylist,
                        onSelectPlaylist = { selectedPlaylist = it },
                        onCreatePlaylist = { name ->
                            scope.launch {
                                db.playlistDao().upsert(Playlist(UUID.randomUUID().toString(), name))
                            }
                        },
                        onPlay = { playSong(it, songs) },
                        onDownload = { OfflineManager.download(context, it) },
                        db = db
                    )
                    3 -> SettingsScreen(
                        gyroEnabled = gyroEnabled,
                        onGyro = { gyroEnabled = it; Prefs.setGyro(context, it) },
                        songCount = songs.size,
                        offlineCount = OfflineManager.offlineCount(context),
                        accentColor = accentColor,
                        onAccent = { accentColor = it; Prefs.setAccentIndex(context, listOf(Color(0xFF746BE8), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)).indexOf(it).coerceAtLeast(0)) },
                        reduceMotion = reduceMotion,
                        onReduceMotion = { reduceMotion = it; Prefs.setReduceMotion(context, it) },
                        onScanLocal = {
                            val perm = if (Build.VERSION.SDK_INT >= 33)
                                Manifest.permission.READ_MEDIA_AUDIO
                            else Manifest.permission.READ_EXTERNAL_STORAGE
                            if (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    val local = LocalMusicScanner.scanDevice(context)
                                    db.songDao().upsertAll(local)
                                    Toast.makeText(context, "${local.size} canciones locales", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(perm)
                            }
                        },
                        onExport = { exportLauncher.launch("kokorofy-backup.json") },
                        onImport = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        onCheckUpdate = {
                            scope.launch {
                                val u = UpdateChecker.check(BuildConfigVersion.NAME)
                                if (u != null) updateInfo = u
                                else Toast.makeText(context, "Ya tienes la última versión", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // FULL PLAYER — fondo opaco (NO se ve el home)
        AnimatedVisibility(
            visible = showFullPlayer && current != null,
            enter = if (reduceMotion) {
                fadeIn(tween(200))
            } else {
                slideInVertically(
                    animationSpec = tween(380, easing = FastOutSlowInEasing),
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(tween(280))
            },
            exit = if (reduceMotion) {
                fadeOut(tween(180))
            } else {
                slideOutVertically(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    targetOffsetY = { fullHeight -> fullHeight }
                ) + fadeOut(tween(220))
            },
            modifier = Modifier.fillMaxSize().zIndex(10f)
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
                    accent = accentColor,
                    onClose = { showFullPlayer = false },
                    onLoadLyrics = {
                        scope.launch {
                            val r = LyricsRepository().fetch(song)
                            lyricsText = r?.synced ?: r?.plain
                        }
                    },
                    onLyricsFull = {
                        if (lyricsText == null) {
                            scope.launch {
                                val r = LyricsRepository().fetch(song)
                                lyricsText = r?.synced ?: r?.plain
                            }
                        }
                        showLyricsFull = true
                    },
                    onDownload = { OfflineManager.download(context, song) },
                    onMore = { showAddToPlaylist = true },
                    onSeek = { controller?.seekTo(it) },
                    onPrev = { controller?.seekToPreviousMediaItem() },
                    onNext = { controller?.seekToNextMediaItem() },
                    onShuffle = {
                        controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false)
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
                        controller?.let { if (it.isPlaying) it.pause() else it.play() }
                    }
                )
            }
        }

        // Lyrics fullscreen — opaco, sin UI del player
        AnimatedVisibility(
            visible = showLyricsFull && current != null,
            enter = fadeIn() + scaleIn(initialScale = 0.97f),
            exit = fadeOut() + scaleOut(targetScale = 0.97f),
            modifier = Modifier.fillMaxSize().zIndex(20f)
        ) {
            current?.let { song ->
                LyricsFullscreen(
                    song = song,
                    lyricsText = lyricsText,
                    position = position,
                    accent = accentColor,
                    onClose = { showLyricsFull = false }
                )
            }
        }

        // Add to playlist sheet
        if (showAddToPlaylist && current != null) {
            AlertDialog(
                onDismissRequest = { showAddToPlaylist = false },
                title = { Text("Agregar a playlist") },
                text = {
                    if (playlists.isEmpty()) {
                        Text("Crea una playlist en Biblioteca primero.")
                    } else {
                        Column {
                            playlists.forEach { pl ->
                                TextButton(onClick = {
                                    scope.launch {
                                        db.playlistDao().addSong(
                                            PlaylistSong(pl.id, current!!.id)
                                        )
                                        Toast.makeText(context, "Añadida a «${pl.name}»", Toast.LENGTH_SHORT).show()
                                        showAddToPlaylist = false
                                    }
                                }) { Text(pl.name) }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddToPlaylist = false }) { Text("Cerrar") }
                }
            )
        }

        // Update popup
        
        if (showChangelog) {
            AlertDialog(
                onDismissRequest = {
                    showChangelog = false
                    Changelog.markShown(context)
                },
                title = { Text("Novedades ${Changelog.CURRENT}") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Changelog.entries.forEach { line ->
                            Text("• $line", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showChangelog = false
                        Changelog.markShown(context)
                    }) { Text("Genial") }
                }
            )
        }

        updateInfo?.let { u ->
            AlertDialog(
                onDismissRequest = { updateInfo = null },
                title = { Text("Actualización disponible") },
                text = {
                    Column {
                        Text("Versión ${u.tag}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(u.body.take(400).ifBlank { u.name }, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Instala el APK nuevo sin desinstalar: tus datos y descargas se conservan.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val url = u.apkUrl ?: u.htmlUrl
                        UpdateChecker.openUrl(context, url)
                        updateInfo = null
                    }) { Text("Actualizar") }
                },
                dismissButton = {
                    TextButton(onClick = { updateInfo = null }) { Text("Después") }
                }
            )
        }
    }
}

/* ── Screens ── */

@Composable
fun HomeScreen(songs: List<Song>, accent: Color, onPlay: (Song) -> Unit, onDownload: (Song) -> Unit) {
    val ctx = LocalContext.current
    val page = Prefs.pageSize(ctx).coerceIn(10, 50)
    var visible by remember { mutableIntStateOf(page) }
    LaunchedEffect(songs.size) {
        visible = page
        while (visible < songs.size) {
            kotlinx.coroutines.delay(80)
            visible = (visible + page).coerceAtMost(songs.size)
        }
    }
    val shown = remember(songs, visible) { songs.take(visible) }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = R.drawable.kokorofy_icon, contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(10.dp))
            Text("KokoroFy", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Spacer(Modifier.height(18.dp))
        Text("PARA TI", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(
            "Escucha lo que te gusta.",
            fontSize = if (Prefs.largeTitles(ctx)) 30.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.8).sp
        )
        Text("Catálogo en vivo · ${shown.size}/${songs.size}", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(14.dp))
        if (songs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent)
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(if (Prefs.compactLists(ctx)) 4.dp else 8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(shown, key = { it.id }) { song ->
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(280)) + slideInVertically(
                        animationSpec = tween(280),
                        initialOffsetY = { it / 6 }
                    )
                ) {
                    TrackRow(song, accent, { onPlay(song) }, { onDownload(song) })
                }
            }
            if (visible < songs.size) {
                item {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = accent)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    songs: List<Song>, query: String, onQuery: (String) -> Unit,
    onPlay: (Song) -> Unit, onDownload: (Song) -> Unit
) {
    val filtered = remember(songs, query) {
        if (query.isBlank()) emptyList()
        else songs.filter { "${it.title} ${it.artist} ${it.album}".contains(query, true) }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Buscar", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(18.dp)) {
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth().padding(4.dp),
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
        when {
            query.isBlank() -> Text("Escribe para buscar", color = Color.Gray, modifier = Modifier.padding(top = 40.dp))
            filtered.isEmpty() -> Text("Sin resultados", color = Color.Gray)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { TrackRow(it, Purple, { onPlay(it) }, { onDownload(it) }) }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    songs: List<Song>, playlists: List<Playlist>, selected: Playlist?,
    onSelectPlaylist: (Playlist?) -> Unit, onCreatePlaylist: (String) -> Unit,
    onPlay: (Song) -> Unit, onDownload: (Song) -> Unit, db: AppDatabase
) {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val playlistSongs by if (selected != null)
        db.playlistDao().observePlaylistSongs(selected.id).collectAsState(emptyList())
    else remember { mutableStateOf(emptyList()) }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected != null) {
                IconButton(onClick = { onSelectPlaylist(null) }) { Icon(Icons.Default.ArrowBack, null) }
            }
            Text(selected?.name ?: "Biblioteca", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (selected == null) {
                IconButton(onClick = { showCreate = true }) { Icon(Icons.Default.PlaylistAdd, null) }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (selected == null) {
            Text("PLAYLISTS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(playlists, key = { it.id }) { pl ->
                    LiquidGlass(corner = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth().clickable { onSelectPlaylist(pl) }.padding(14.dp),
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
                    Text("TODAS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
                items(songs, key = { "a-${it.id}" }) { TrackRow(it, Purple, { onPlay(it) }, { onDownload(it) }) }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (playlistSongs.isEmpty()) item { Text("Playlist vacía", color = Color.Gray) }
                items(playlistSongs, key = { it.id }) { TrackRow(it, Purple, { onPlay(it) }, { onDownload(it) }) }
            }
        }
    }
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Nueva playlist") },
            text = { OutlinedTextField(newName, { newName = it }, singleLine = true, label = { Text("Nombre") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        onCreatePlaylist(newName.trim()); newName = ""; showCreate = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun SettingsScreen(
    gyroEnabled: Boolean, onGyro: (Boolean) -> Unit,
    songCount: Int, offlineCount: Int,
    accentColor: Color, onAccent: (Color) -> Unit,
    reduceMotion: Boolean, onReduceMotion: (Boolean) -> Unit,
    onScanLocal: () -> Unit, onExport: () -> Unit, onImport: () -> Unit, onCheckUpdate: () -> Unit
) {
    val accents = listOf(
        Color(0xFF746BE8), Color(0xFFEC4899), Color(0xFF06B6D4),
        Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)
    )
    Column(Modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState())) {
        Text("Ajustes", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))

        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("EXPERIMENTAL", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Efecto 3D cinemático", fontWeight = FontWeight.SemiBold)
                        Text("Parallax suave con giroscopio / gravedad", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(checked = gyroEnabled, onCheckedChange = onGyro)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Reducir animaciones", fontWeight = FontWeight.SemiBold)
                        Text("Menos motion si prefieres", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(checked = reduceMotion, onCheckedChange = onReduceMotion)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("PERSONALIZACIÓN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Color de acento", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    accents.forEach { c ->
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(c)
                                .border(if (c == accentColor) 3.dp else 0.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                .clickable { onAccent(c) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        val ctx = LocalContext.current
        var speed by remember { mutableFloatStateOf(Prefs.playbackSpeed(ctx)) }
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("AUDIO", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Velocidad: ${"%.2f".format(speed)}x", fontWeight = FontWeight.SemiBold)
                Slider(
                    value = speed,
                    onValueChange = {
                        speed = it
                        Prefs.setPlaybackSpeed(ctx, it)
                    },
                    valueRange = 0.5f..2f,
                    steps = 5
                )
                Text("0.5x — 1x — 1.5x — 2x", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("PRIVACIDAD (10)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                PrivacyToggle(ctx, "Sin analíticas", Prefs.analyticsOff(ctx)) { Prefs.setAnalyticsOff(ctx, it) }
                PrivacyToggle(ctx, "Ocultar historial", Prefs.hideHistory(ctx)) { Prefs.setHideHistory(ctx, it) }
                PrivacyToggle(ctx, "Sesión privada", Prefs.privateSession(ctx)) { Prefs.setPrivateSession(ctx, it) }
                PrivacyToggle(ctx, "Bloquear capturas", Prefs.blockScreenshots(ctx)) { Prefs.setBlockScreenshots(ctx, it) }
                PrivacyToggle(ctx, "Limpiar al salir", Prefs.clearOnExit(ctx)) { Prefs.setClearOnExit(ctx, it) }
                PrivacyToggle(ctx, "Sin backup en la nube", Prefs.noCloudBackup(ctx)) { Prefs.setNoCloudBackup(ctx, it) }
                PrivacyToggle(ctx, "Ocultar carátula en notificación", Prefs.hideNotificationArt(ctx)) { Prefs.setHideNotificationArt(ctx, it) }
                PrivacyToggle(ctx, "Pedir biometría (flag)", Prefs.requireBiometric(ctx)) { Prefs.setRequireBiometric(ctx, it) }
                PrivacyToggle(ctx, "Limitar red medida", Prefs.limitNetworkMetered(ctx)) { Prefs.setLimitNetworkMetered(ctx, it) }
                PrivacyToggle(ctx, "No guardar búsquedas", Prefs.redactSearch(ctx)) { Prefs.setRedactSearch(ctx, it) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    val dir = java.io.File(ctx.filesDir, "offline")
                    dir.listFiles()?.forEach { it.delete() }
                    java.io.File(ctx.filesDir, "music_cache").deleteRecursively()
                    android.widget.Toast.makeText(ctx, "Caché y offline borrados", android.widget.Toast.LENGTH_SHORT).show()
                }) { Text("Borrar datos offline") }
            }
        }

        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("MÚSICA LOCAL", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Escanea Music / Audio del teléfono vía MediaStore", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp))
                Button(onClick = onScanLocal) {
                    Icon(Icons.Default.FolderOpen, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Escanear dispositivo")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("RESPALDO", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Exporta / importa playlists y catálogo (JSON)", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onExport) { Text("Exportar") }
                    OutlinedButton(onClick = onImport) { Text("Importar") }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("ACTUALIZACIONES", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Versión actual: ${BuildConfigVersion.NAME}", fontSize = 14.sp)
                Text("Instala el APK nuevo sin desinstalar para conservar datos.", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp))
                Button(onClick = onCheckUpdate) {
                    Icon(Icons.Default.SystemUpdate, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Buscar actualización")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("CATÁLOGO", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("$songCount canciones · $offlineCount offline", fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

/* ── Components ── */

@Composable
fun TrackRow(song: Song, accent: Color, onPlay: () -> Unit, onDownload: () -> Unit) {
    val ctx = LocalContext.current
    val offline = OfflineManager.isDownloaded(ctx, song.id)
    LiquidGlass(corner = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onPlay).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl ?: R.drawable.kokorofy_icon, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = Color.Gray, fontSize = 13.sp, maxLines = 1)
            }
            IconButton(onClick = onDownload) {
                Icon(
                    if (offline) Icons.Filled.DownloadDone else Icons.Outlined.DownloadForOffline,
                    "Descargar",
                    tint = if (offline) accent else TextDark
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song, isPlaying: Boolean, position: Long, duration: Long, accent: Color,
    onOpen: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit
) {
    val progress = if (duration > 0) position.toFloat() / duration else 0f
    LiquidGlass(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clickable(onClick = onOpen),
        corner = RoundedCornerShape(18.dp)
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = accent, trackColor = Color(0x22000000)
            )
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = song.coverUrl ?: R.drawable.kokorofy_icon, contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                IconButton(onClick = onPlayPause) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null)
                }
                IconButton(onClick = onNext) { Icon(Icons.Filled.SkipNext, null) }
            }
        }
    }
}

@Composable
fun FullPlayer(
    song: Song, controller: MediaController?, isPlaying: Boolean,
    position: Long, duration: Long, lyricsText: String?,
    tiltX: Float, tiltY: Float, gyroEnabled: Boolean, accent: Color,
    onClose: () -> Unit, onLoadLyrics: () -> Unit, onLyricsFull: () -> Unit,
    onDownload: () -> Unit, onMore: () -> Unit,
    onSeek: (Long) -> Unit, onPrev: () -> Unit, onNext: () -> Unit,
    onShuffle: () -> Unit, onRepeat: () -> Unit, onPlayPause: () -> Unit
) {
    // Capa OPACA completa — no se ve el home debajo
    Box(Modifier.fillMaxSize().background(Color(0xFF0C0C10))) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon, contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(48.dp).graphicsLayer {
                alpha = 0.5f
                if (gyroEnabled) {
                    translationX = tiltX * 2.5f
                    translationY = tiltY * 2.5f
                    scaleX = 1.15f; scaleY = 1.15f
                }
            }
        )
        Box(Modifier.fillMaxSize().background(Color(0xAA000000)))

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("REPRODUCIENDO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xCCFFFFFF))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onMore) {
                    Icon(Icons.Default.MoreVert, "Más", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            AsyncImage(
                model = song.coverUrl ?: R.drawable.kokorofy_icon, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(28.dp))
                    .graphicsLayer {
                        if (gyroEnabled) {
                            rotationX = tiltY * 0.4f
                            rotationY = -tiltX * 0.4f
                            cameraDistance = 14f * density
                        }
                    }
            )
            Spacer(Modifier.height(20.dp))
            Text(song.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(song.artist, fontSize = 16.sp, color = Color(0xCCFFFFFF))
            Spacer(Modifier.height(10.dp))

            Slider(
                value = if (duration > 0) position.toFloat() / duration else 0f,
                onValueChange = { onSeek((it * duration).toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, activeTrackColor = Color.White,
                    inactiveTrackColor = Color(0x44FFFFFF)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position), fontSize = 12.sp, color = Color(0xAAFFFFFF))
                Text(formatTime(duration), fontSize = 12.sp, color = Color(0xAAFFFFFF))
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(Icons.Default.Shuffle, null,
                        tint = if (controller?.shuffleModeEnabled == true) accent else Color.White)
                }
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick = onPlayPause, modifier = Modifier.size(68.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = TextDark, modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = onRepeat) {
                    val mode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
                    Icon(
                        if (mode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        null, tint = if (mode != Player.REPEAT_MODE_OFF) accent else Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = onDownload) {
                    Icon(Icons.Outlined.DownloadForOffline, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Offline", color = Color.White)
                }
                TextButton(onClick = onLyricsFull) {
                    Icon(Icons.Default.Lyrics, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Letras", color = Color.White)
                }
            }

            Spacer(Modifier.height(14.dp))
            LiquidGlass(corner = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("LETRAS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            if (lyricsText == null) onLoadLyrics()
                            onLyricsFull()
                        }) { Text("Fullscreen") }
                    }
                    if (lyricsText == null) {
                        TextButton(onClick = onLoadLyrics) { Text("Cargar letras") }
                    } else {
                        KaraokePreview(lyricsText!!, position, accent)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun KaraokePreview(lyrics: String, position: Long, accent: Color) {
    val lines = remember(lyrics) { parseLrc(lyrics) }
    if (lines.isEmpty()) {
        Text(lyrics, fontSize = 16.sp, lineHeight = 26.sp, maxLines = 6, overflow = TextOverflow.Ellipsis)
        return
    }
    val active = lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    Column {
        lines.drop(maxOf(0, active - 1)).take(5).forEach { line ->
            val isActive = lines.indexOf(line) == active
            val scale by animateFloatAsState(if (isActive) 1.05f else 1f, label = "ks")
            Text(
                line.text,
                fontSize = if (isActive) 18.sp else 15.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) accent else Color.Gray,
                modifier = Modifier.padding(vertical = 3.dp).graphicsLayer { scaleX = scale; scaleY = scale }
            )
        }
    }
}

@Composable
fun LyricsFullscreen(
    song: Song, lyricsText: String?, position: Long, accent: Color, onClose: () -> Unit
) {
    val lines = remember(lyricsText) { parseLrc(lyricsText ?: "") }
    val listState = rememberLazyListState()
    val active = if (lines.isEmpty()) -1 else lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)

    LaunchedEffect(active) {
        if (active >= 0) runCatching { listState.animateScrollToItem(maxOf(0, active - 2)) }
    }

    // OPACO — no se ve el player debajo
    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0E))) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon, contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(56.dp).graphicsLayer { alpha = 0.4f }
        )
        Box(Modifier.fillMaxSize().background(Color(0xBB000000)))

        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(song.artist, color = Color(0xAAFFFFFF), fontSize = 13.sp)
                }
            }
            when {
                lyricsText.isNullOrBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin letras", color = Color.White)
                }
                lines.isEmpty() -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
                ) {
                    Text(lyricsText!!, color = Color.White, fontSize = 22.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
                }
                else -> LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(lines.size) { i ->
                        val line = lines[i]
                        val isActive = i == active
                        val alpha by animateFloatAsState(if (isActive) 1f else 0.35f, label = "la")
                        val scale by animateFloatAsState(if (isActive) 1.08f else 1f, tween(300), label = "ls")
                        Text(
                            line.text,
                            fontSize = if (isActive) 28.sp else 18.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) Color.White else Color.White.copy(alpha = alpha),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyToggle(ctx: android.content.Context, label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    var v by remember { mutableStateOf(value) }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = v, onCheckedChange = { v = it; onChange(it) })
    }
}

fun formatTime(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}

fun parseLrc(raw: String): List<LyricLine> {
    if (raw.isBlank()) return emptyList()
    val regex = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]\s*(.*)""")
    val out = mutableListOf<LyricLine>()
    raw.lineSequence().forEach { line ->
        val m = regex.find(line.trim()) ?: return@forEach
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
        if (text.isNotEmpty()) out += LyricLine(min * 60_000 + sec * 1000 + frac, text)
    }
    return out.sortedBy { it.timeMs }
}

/** Evita depender de BuildConfig generado en todos los entornos. */
object BuildConfigVersion {
    const val NAME = "1.3.0"
}
