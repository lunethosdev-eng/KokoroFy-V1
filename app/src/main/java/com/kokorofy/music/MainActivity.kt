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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontFamily
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

object BuildConfigVersion {
    const val NAME = "1.5.1"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            var dark by remember { mutableStateOf(Prefs.themeMode(ctx) == 1) }
            KokoroTheme(dark = dark) {
                KokoroFyRoot(
                    dark = dark,
                    onDarkChange = {
                        dark = it
                        Prefs.setThemeMode(ctx, if (it) 1 else 0)
                    }
                )
            }
        }
    }
}

/* ─── Colors ─── */
private val Purple = Color(0xFFB8A7FF)
private val SpotGreen = Color(0xFF1ED760)
private val LightBg = Color(0xFFF7F7F5)
private val DarkBg = Color(0xFF0B0B0D)
private val DarkSurface = Color(0xFF151518)

@Composable
fun KokoroTheme(dark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (dark) {
        darkColorScheme(
            background = DarkBg,
            surface = DarkSurface,
            surfaceVariant = Color(0xFF202024),
            primary = SpotGreen,
            secondary = Color(0xFFBDB8FF),
            tertiary = Color(0xFFFFB4AB),
            onBackground = Color(0xFFF7F7F8),
            onSurface = Color(0xFFF7F7F8),
            onSurfaceVariant = Color(0xFFBDBDC3)
        )
    } else {
        lightColorScheme(
            background = LightBg,
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFECEBEF),
            primary = Purple,
            secondary = Color(0xFF6750A4),
            onBackground = Color(0xFF17171A),
            onSurface = Color(0xFF17171A),
            onSurfaceVariant = Color(0xFF66656C)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

/**
 * Layered liquid-glass surface.
 * It deliberately uses several translucent layers, edge highlights and a moving
 * specular reflection instead of a single opaque card, so the background artwork
 * remains visible through the surface.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(22.dp),
    intense: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background == DarkBg
    val infinite = rememberInfiniteTransition(label = "glass")
    val shine by infinite.animateFloat(
        initialValue = -0.25f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "glassShine"
    )
    val tint = if (dark) Color.White else Color.White
    val baseAlpha = if (dark) if (intense) 0.16f else 0.10f else if (intense) 0.58f else 0.40f
    val highlight = if (dark) 0.22f else 0.48f

    Box(
        modifier
            .clip(corner)
            .background(tint.copy(alpha = baseAlpha))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = highlight),
                        Color.White.copy(alpha = if (dark) 0.07f else 0.20f),
                        Color.Black.copy(alpha = if (dark) 0.20f else 0.04f)
                    )
                ),
                corner
            )
    ) {
        // Soft internal diffusion gives the glass body depth.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (dark) 0.08f else 0.24f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (dark) 0.10f else 0.025f)
                        )
                    )
                )
        )
        // Moving specular reflection.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (dark) 0.08f else 0.16f),
                            Color.Transparent
                        ),
                        start = Offset(shine * 900f - 300f, -100f),
                        end = Offset(shine * 900f + 120f, 700f)
                    )
                )
        )
        content()
    }
}

@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color(0xFF1ED760) else Color.White.copy(alpha = 0.08f)
    val fg = if (selected) Color.Black else Color.White.copy(alpha = 0.92f)
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(0.8.dp, if (selected) Color(0xFF1ED760) else Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GlassIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LiquidGlass(
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        corner = RoundedCornerShape(14.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

/* ─── Root ─── */
@Composable
fun KokoroFyRoot(dark: Boolean, onDarkChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val repo = remember { CatalogRepository(context) }
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

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
    var gyroEnabled by remember { mutableStateOf(Prefs.gyro(context)) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showAddToPlaylist by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdate?>(null) }
    var showChangelog by remember { mutableStateOf(false) }
    var accentColor by remember { mutableStateOf(SpotGreen) }
    var reduceMotion by remember { mutableStateOf(Prefs.reduceMotion(context)) }
    val downloadProgress by OfflineManager.progress.collectAsState()
    var homeFilter by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val accents = listOf(
            Color(0xFF8B7CFF), Color(0xFFEC4899), Color(0xFF06B6D4),
            Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)
        )
        accentColor = accents.getOrElse(Prefs.accentIndex(context)) { accents[0] }
        if (Changelog.shouldShow(context)) showChangelog = true
    }

    LaunchedEffect(Unit) {
        while (true) {
            runCatching { repo.refresh() }
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
                c.setPlaybackSpeed(Prefs.speed(context))
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

    LaunchedEffect(current?.id, isPlaying) {
        current?.let { PlayerWidget.sync(context, it.title, it.artist, isPlaying) }
    }

    LaunchedEffect(current?.id) {
        lyricsText = null
        val song = current ?: return@LaunchedEffect
        song.lyrics?.takeIf { it.isNotBlank() }?.let {
            lyricsText = it
            return@LaunchedEffect
        }
        // Fallback LRCLIB / repo
        runCatching {
            val result = LyricsRepository().fetch(song)
            val text = result?.synced?.takeIf { it.isNotBlank() }
                ?: result?.plain?.takeIf { it.isNotBlank() }
            if (!text.isNullOrBlank()) lyricsText = text
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
                        lyricsText = null
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

    DisposableEffect(gyroEnabled) {
        if (!gyroEnabled) {
            tiltX = 0f; tiltY = 0f
            return@DisposableEffect onDispose {}
        }
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            private var sx = 0f
            private var sy = 0f
            override fun onSensorChanged(e: SensorEvent) {
                val rx = (e.values.getOrNull(0) ?: 0f) * 3f
                val ry = (e.values.getOrNull(1) ?: 0f) * 3f
                sx = sx * 0.85f + rx * 0.15f
                sy = sy * 0.85f + ry * 0.15f
                tiltX = sx.coerceIn(-20f, 20f)
                tiltY = sy.coerceIn(-20f, 20f)
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
            MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(OfflineManager.playUri(context, s))
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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val local = LocalMusicScanner.scanDevice(context)
                if (local.isNotEmpty()) {
                    db.songDao().upsertAll(local)
                    Toast.makeText(context, "${local.size} locales", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(context, "Sin música local", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) scope.launch {
            BackupManager.writeToUri(context, uri, BackupManager.exportAll(context, songs, playlists))
            Toast.makeText(context, "Exportado", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch {
            val n = BackupManager.importJson(context, BackupManager.readFromUri(context, uri))
            Toast.makeText(context, "Importadas $n", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Layout fijo: lista NUNCA debajo del bottomBar ──
    Box(
        Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        DownloadProgressOverlay(downloadProgress)
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // Solo mini player + dock — fuera del LazyColumn
                Column(Modifier.fillMaxWidth()) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        corner = RoundedCornerShape(28.dp),
                        intense = true
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(64.dp)
                        ) {
                            listOf(
                                Triple(0, "Inicio", Icons.Filled.Home),
                                Triple(1, "Buscar", Icons.Filled.Search),
                                Triple(2, "Biblioteca", Icons.Filled.LibraryMusic),
                                Triple(3, "Ajustes", Icons.Filled.Settings)
                            ).forEach { (i, name, icon) ->
                                NavigationBarItem(
                                    selected = tab == i,
                                    onClick = { tab = i; selectedPlaylist = null },
                                    icon = { Icon(icon, name, modifier = Modifier.size(22.dp)) },
                                    label = { Text(name, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = accentColor.copy(alpha = 0.25f),
                                        selectedIconColor = accentColor,
                                        selectedTextColor = accentColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { pad ->
            // Content con padding del Scaffold → no se mete bajo el dock
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                when (tab) {
                    0 -> HomeScreen(
                        songs = songs,
                        playlists = playlists,
                        filter = homeFilter,
                        onFilter = { homeFilter = it },
                        accent = accentColor,
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
                        onSelect = { selectedPlaylist = it },
                        onCreate = { name ->
                            scope.launch {
                                db.playlistDao().upsert(Playlist(UUID.randomUUID().toString(), name))
                            }
                        },
                        onPlay = { playSong(it, songs) },
                        onDownload = { OfflineManager.download(context, it) },
                        db = db
                    )
                    3 -> SettingsScreen(
                        dark = dark,
                        onDark = onDarkChange,
                        gyro = gyroEnabled,
                        onGyro = { gyroEnabled = it; Prefs.setGyro(context, it) },
                        reduceMotion = reduceMotion,
                        onReduceMotion = { reduceMotion = it; Prefs.setReduceMotion(context, it) },
                        accent = accentColor,
                        onAccent = { c ->
                            accentColor = c
                            val list = listOf(
                                Color(0xFF8B7CFF), Color(0xFFEC4899), Color(0xFF06B6D4),
                                Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)
                            )
                            Prefs.setAccentIndex(context, list.indexOf(c).coerceAtLeast(0))
                        },
                        songCount = songs.size,
                        offlineCount = OfflineManager.offlineCount(context),
                        onScan = {
                            val p = if (Build.VERSION.SDK_INT >= 33)
                                Manifest.permission.READ_MEDIA_AUDIO
                            else Manifest.permission.READ_EXTERNAL_STORAGE
                            if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    val local = LocalMusicScanner.scanDevice(context)
                                    db.songDao().upsertAll(local)
                                    Toast.makeText(context, "${local.size} locales", Toast.LENGTH_SHORT).show()
                                }
                            } else permissionLauncher.launch(p)
                        },
                        onExport = { exportLauncher.launch("kokorofy-backup.json") },
                        onImport = { importLauncher.launch(arrayOf("application/json")) },
                        onCheckUpdate = {
                            scope.launch {
                                val u = UpdateChecker.check(BuildConfigVersion.NAME)
                                if (u != null) updateInfo = u
                                else Toast.makeText(context, "Sin actualizaciones", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // Full player (opaco + blur cover)
        AnimatedVisibility(
            visible = showFullPlayer && current != null,
            enter = if (reduceMotion) fadeIn() else slideInVertically(
                animationSpec = tween(380, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ) + fadeIn(tween(280)),
            exit = if (reduceMotion) fadeOut() else slideOutVertically(
                animationSpec = tween(320),
                targetOffsetY = { it }
            ) + fadeOut(tween(220)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(20f)
        ) {
            current?.let { song ->
                FullPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    position = position,
                    duration = duration,
                    accent = accentColor,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    lyrics = lyricsText,
                    onClose = { showFullPlayer = false },
                    onPlayPause = { controller?.let { if (it.isPlaying) it.pause() else it.play() } },
                    onSeek = { controller?.seekTo(it) },
                    onPrev = { controller?.seekToPreviousMediaItem() },
                    onNext = { controller?.seekToNextMediaItem() },
                    onLyrics = { showLyricsFull = true },
                    onAddPlaylist = { showAddToPlaylist = true },
                    onDownload = { OfflineManager.download(context, song) }
                )
            }
        }

        if (showLyricsFull && current != null) {
            LyricsFullscreen(
                song = current!!,
                lyrics = lyricsText,
                position = position,
                accent = accentColor,
                onClose = { showLyricsFull = false }
            )
        }

        if (showAddToPlaylist && current != null) {
            AlertDialog(
                onDismissRequest = { showAddToPlaylist = false },
                title = { Text("Añadir a playlist") },
                text = {
                    Column {
                        if (playlists.isEmpty()) Text("Crea una playlist en Biblioteca")
                        playlists.forEach { pl ->
                            TextButton(onClick = {
                                scope.launch {
                                    db.playlistDao().addSong(PlaylistSong(playlistId = pl.id, songId = current!!.id, position = 0))
                                    Toast.makeText(context, "Añadida a ${pl.name}", Toast.LENGTH_SHORT).show()
                                    showAddToPlaylist = false
                                }
                            }) { Text(pl.name) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddToPlaylist = false }) { Text("Cerrar") }
                }
            )
        }

        if (showChangelog) {
            AlertDialog(
                onDismissRequest = {
                    showChangelog = false
                    Changelog.markShown(context)
                },
                title = { Text("Novedades ${Changelog.CURRENT}") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Changelog.entries.forEach {
                            Text("• $it", fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
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
                title = { Text("Actualización ${u.tag}") },
                text = { Text(u.body.ifBlank { "Hay una nueva versión disponible." }) },
                confirmButton = {
                    TextButton(onClick = { updateInfo = null }) { Text("OK") }
                }
            )
        }
    }
}

/* ─── Screens ─── */

@Composable
fun HomeScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    filter: Int,
    onFilter: (Int) -> Unit,
    accent: Color,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val featured = songs.firstOrNull()
    val recent = songs.take(12)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("K", fontWeight = FontWeight.Bold, color = cs.onBackground, fontSize = 14.sp)
                }
                GlassChip("All", filter == 0, { onFilter(0) })
                GlassChip("Music", filter == 1, { onFilter(1) })
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Notifications, null, tint = cs.onBackground.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            val cards = buildList {
                add(Triple("Liked Songs", songs.firstOrNull()?.coverUrl, true))
                playlists.take(3).forEach { add(Triple(it.name, it.coverUrl ?: songs.firstOrNull()?.coverUrl, false)) }
                songs.take(4).forEach { add(Triple(it.title, it.coverUrl, false)) }
            }
            Column(Modifier.padding(horizontal = 14.dp)) {
                cards.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (title, cover, liked) ->
                            LiquidGlass(
                                modifier = Modifier.weight(1f).height(56.dp).clickable {
                                    songs.firstOrNull { s -> s.title == title || liked }?.let(onPlay)
                                        ?: songs.firstOrNull()?.let(onPlay)
                                },
                                corner = RoundedCornerShape(10.dp),
                                intense = true
                            ) {
                                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.width(56.dp).fillMaxHeight().background(
                                            if (liked) accent else cs.surface
                                        )
                                    ) {
                                        if (!liked && cover != null) {
                                            AsyncImage(model = cover, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        } else if (liked) {
                                            Icon(Icons.Filled.Favorite, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(22.dp))
                                        }
                                    }
                                    Text(
                                        title,
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = cs.onBackground
                                    )
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (featured != null) {
            item {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = featured.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("New release from", fontSize = 11.sp, color = cs.onBackground.copy(alpha = 0.5f))
                        Text(featured.artist, fontWeight = FontWeight.Bold, color = cs.onBackground, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                LiquidGlass(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).clickable { onPlay(featured) },
                    corner = RoundedCornerShape(18.dp),
                    intense = true
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = featured.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Single", fontSize = 11.sp, color = cs.onBackground.copy(alpha = 0.5f))
                            Text(featured.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = cs.onBackground, maxLines = 2)
                            Text(featured.artist, fontSize = 13.sp, color = cs.onBackground.copy(alpha = 0.6f))
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GlassIconButton(onClick = { onDownload(featured) }) {
                                    Icon(Icons.Filled.Add, null, tint = cs.onBackground, modifier = Modifier.size(18.dp))
                                }
                                GlassIconButton(onClick = { onPlay(featured) }) {
                                    Icon(Icons.Filled.PlayArrow, null, tint = accent, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(
                "New releases for you",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = cs.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recent, key = { it.id }) { song ->
                    Column(Modifier.width(140.dp).clickable { onPlay(song) }) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(12.dp)).background(cs.surface)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = cs.onBackground, fontSize = 13.sp)
                        Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = cs.onBackground.copy(alpha = 0.55f), fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
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
    val cs = MaterialTheme.colorScheme
    val accent = MaterialTheme.colorScheme.primary
    val filtered = remember(query, songs) {
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, true) || it.artist.contains(query, true)
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Buscar", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.onBackground)
        Spacer(Modifier.height(12.dp))
        LiquidGlass(corner = RoundedCornerShape(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                placeholder = { Text("Canciones, artistas…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { song ->
                TrackRow(song, accent, { onPlay(song) }, { onDownload(song) })
            }
            if (filtered.isEmpty() && query.isNotBlank()) {
                item {
                    Text(
                        "Sin resultados locales.\n(Cuando despliegues Seki en Render, aquí se podrá buscar online.)",
                        color = cs.onBackground.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
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
    onSelect: (Playlist?) -> Unit,
    onCreate: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    db: AppDatabase
) {
    val cs = MaterialTheme.colorScheme
    val accent = MaterialTheme.colorScheme.primary
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(selected?.id) {
        val pl = selected ?: run {
            playlistSongs = emptyList()
            return@LaunchedEffect
        }
        try {
            db.playlistDao().observePlaylistSongs(pl.id).collect { playlistSongs = it }
        } catch (_: Throwable) {
            playlistSongs = emptyList()
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Biblioteca", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.onBackground)
        Spacer(Modifier.height(12.dp))

        if (selected == null) {
            LiquidGlass(corner = RoundedCornerShape(16.dp)) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Nueva playlist") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    GlassIconButton(onClick = {
                        if (name.isNotBlank()) {
                            onCreate(name.trim())
                            name = ""
                        }
                    }) {
                        Icon(Icons.Filled.Add, null, tint = accent)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(playlists, key = { it.id }) { pl ->
                    LiquidGlass(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(pl) },
                        corner = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.QueueMusic, null, tint = accent)
                            Spacer(Modifier.width(12.dp))
                            Text(pl.name, fontWeight = FontWeight.SemiBold, color = cs.onBackground)
                        }
                    }
                }
                item {
                    Text(
                        "Todas las canciones (${songs.size})",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = cs.onBackground
                    )
                }
                items(songs.take(40), key = { it.id }) { song ->
                    TrackRow(song, accent, { onPlay(song) }, { onDownload(song) })
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(onClick = { onSelect(null) }) {
                    Icon(Icons.Filled.ArrowBack, null, tint = cs.onBackground)
                }
                Spacer(Modifier.width(10.dp))
                Text(selected.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = cs.onBackground)
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(playlistSongs, key = { it.id }) { song ->
                    TrackRow(song, accent, { onPlay(song) }, { onDownload(song) })
                }
                if (playlistSongs.isEmpty()) {
                    item {
                        Text("Playlist vacía", color = cs.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    dark: Boolean,
    onDark: (Boolean) -> Unit,
    gyro: Boolean,
    onGyro: (Boolean) -> Unit,
    reduceMotion: Boolean,
    onReduceMotion: (Boolean) -> Unit,
    accent: Color,
    onAccent: (Color) -> Unit,
    songCount: Int,
    offlineCount: Int,
    onScan: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    var speed by remember { mutableFloatStateOf(Prefs.speed(ctx)) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("Ajustes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.onBackground)
            Spacer(Modifier.height(16.dp))
        }

        item {
            SettingsCard("APARIENCIA") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Modo oscuro", Modifier.weight(1f), color = cs.onBackground)
                    Switch(checked = dark, onCheckedChange = onDark)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Menos animaciones", Modifier.weight(1f), color = cs.onBackground)
                    Switch(checked = reduceMotion, onCheckedChange = onReduceMotion)
                }
                Text("Color de acento", fontSize = 13.sp, color = cs.onBackground.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Color(0xFF8B7CFF), Color(0xFFEC4899), Color(0xFF06B6D4),
                        Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)
                    ).forEach { c ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    if (c == accent) 3.dp else 0.dp,
                                    cs.onBackground.copy(alpha = 0.4f),
                                    CircleShape
                                )
                                .clickable { onAccent(c) }
                        )
                    }
                }
            }
        }

        item {
            SettingsCard("AUDIO") {
                Text("Velocidad: ${"%.2f".format(speed)}x", color = cs.onBackground)
                Slider(
                    value = speed,
                    onValueChange = {
                        speed = it
                        Prefs.setSpeed(ctx, it)
                    },
                    valueRange = 0.5f..2f,
                    steps = 5
                )
            }
        }

        item {
            SettingsCard("EXPERIMENTAL") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Efecto 3D (giroscopio)", Modifier.weight(1f), color = cs.onBackground)
                    Switch(checked = gyro, onCheckedChange = onGyro)
                }
            }
        }

        item {
            SettingsCard("DATOS") {
                Text("$songCount canciones · $offlineCount offline", color = cs.onBackground.copy(alpha = 0.7f), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassAction("Escanear local", onScan)
                    GlassAction("Exportar", onExport)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassAction("Importar", onImport)
                    GlassAction("Buscar update", onCheckUpdate)
                }
            }
        }

        item {
            SettingsCard("PRIVACIDAD") {
                PrivacyLine("Sin analíticas", Prefs.analyticsOff(ctx)) { Prefs.setAnalyticsOff(ctx, it) }
                PrivacyLine("Sesión privada", Prefs.privateSession(ctx)) { Prefs.setPrivateSession(ctx, it) }
                PrivacyLine("Ocultar historial", Prefs.hideHistory(ctx)) { Prefs.setHideHistory(ctx, it) }
                PrivacyLine("Limpiar al salir", Prefs.clearOnExit(ctx)) { Prefs.setClearOnExit(ctx, it) }
                Spacer(Modifier.height(6.dp))
                GlassAction("Borrar offline/caché") {
                    java.io.File(ctx.filesDir, "offline").listFiles()?.forEach { it.delete() }
                    java.io.File(ctx.filesDir, "music_cache").deleteRecursively()
                    Toast.makeText(ctx, "Borrado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        item {
            SettingsCard("VERSIONES") {
                Text(
                    "Si no te gusta esta UI, puedes instalar una versión anterior desde GitHub Releases sin perder la firma (misma keystore).",
                    fontSize = 13.sp,
                    color = cs.onBackground.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                GlassAction("Abrir versiones anteriores") {
                    try {
                        val i = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/lunethosdev-eng/KokoroFy-V1/releases")
                        )
                        ctx.startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(ctx, "Abre github.com/lunethosdev-eng/KokoroFy-V1/releases", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(
                "Gracias por tu apoyo a este proyecto pequeño",
                fontSize = 13.sp,
                color = cs.onBackground.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Serenx · KokoroFy",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            Text(
                "UI & código asistidos por Grok (xAI) · v${BuildConfigVersion.NAME}",
                fontSize = 11.sp,
                color = cs.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Spacer(Modifier.height(12.dp))
    LiquidGlass(corner = RoundedCornerShape(24.dp), intense = true) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                title,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground.copy(alpha = 0.48f)
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun GlassAction(label: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    LiquidGlass(
        modifier = Modifier.clickable(onClick = onClick),
        corner = RoundedCornerShape(12.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = cs.onBackground
        )
    }
}

@Composable
private fun PrivacyLine(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    var v by remember { mutableStateOf(value) }
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp, color = cs.onBackground)
        Switch(checked = v, onCheckedChange = { v = it; onChange(it) })
    }
}

/* ─── Rows / Player ─── */

@Composable
fun TrackRow(song: Song, accent: Color, onPlay: () -> Unit, onDownload: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    LiquidGlass(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        corner = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.surface)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = cs.onBackground
                )
                Text(
                    song.artist,
                    fontSize = 13.sp,
                    color = cs.onBackground.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            GlassIconButton(onClick = onPlay) {
                Icon(Icons.Filled.PlayArrow, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(6.dp))
            GlassIconButton(onClick = onDownload) {
                Icon(Icons.Filled.Download, null, tint = cs.onBackground.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
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
    accent: Color,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
        LiquidGlass(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
            corner = RoundedCornerShape(20.dp),
            intense = true
        ) {
            Column {
                Row(
                    Modifier.padding(start = 9.dp, end = 7.dp, top = 8.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(song.coverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.06f)))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = cs.onBackground)
                        Text(song.artist, fontSize = 12.sp, color = cs.onBackground.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onPlayPause) {
                        Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = cs.onBackground)
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Filled.SkipNext, null, tint = cs.onBackground)
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = cs.onBackground.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
fun FullPlayer(
    song: Song,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    accent: Color,
    tiltX: Float,
    tiltY: Float,
    lyrics: String?,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onLyrics: () -> Unit,
    onAddPlaylist: () -> Unit,
    onDownload: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.background == DarkBg
    val parsed = remember(lyrics) { parseLrc(lyrics ?: "") }
    val active = parsed.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    Box(Modifier.fillMaxSize().background(if (dark) DarkBg else LightBg)) {
        AsyncImage(
            song.coverUrl, null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(64.dp).graphicsLayer {
                scaleX = 1.16f; scaleY = 1.16f
                translationX = tiltX * 1.4f; translationY = tiltY * 1.4f
            }
        )
        Box(Modifier.fillMaxSize().background(if (dark) Color.Black.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.70f)))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, if (dark) Color.Black.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.25f)))))

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(onClick = onClose) { Icon(Icons.Filled.KeyboardArrowDown, "Cerrar", tint = MaterialTheme.colorScheme.onBackground) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPRODUCIENDO", fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
                    Text("KokoroFy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                }
                GlassIconButton(onClick = onAddPlaylist) { Icon(Icons.Filled.MoreVert, "Más", tint = MaterialTheme.colorScheme.onBackground) }
            }

            Spacer(Modifier.height(22.dp))
            Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(horizontal = 5.dp).graphicsLayer { translationX = tiltX * 0.45f; translationY = tiltY * 0.45f }) {
                AsyncImage(song.coverUrl, null, ContentScale.Crop, Modifier.fillMaxSize().clip(RoundedCornerShape(26.dp)))
                Box(Modifier.matchParentSize().border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(26.dp)))
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onDownload) { Icon(Icons.Filled.Download, "Descargar", tint = accent) }
            }

            Spacer(Modifier.height(12.dp))
            Slider(
                value = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                onValueChange = { onSeek((it * duration).toLong()) },
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent, inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f))
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position), fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f))
                Text(formatTime(duration), fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f))
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(onClick = onPrev, modifier = Modifier.size(50.dp)) { Icon(Icons.Filled.SkipPrevious, "Anterior", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp)) }
                Box(Modifier.size(70.dp).clip(CircleShape).background(accent).clickable(onClick = onPlayPause), contentAlignment = Alignment.Center) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Reproducir", tint = if (accent.luminance() > 0.65f) Color.Black else Color.White, modifier = Modifier.size(36.dp))
                }
                GlassIconButton(onClick = onNext, modifier = Modifier.size(50.dp)) { Icon(Icons.Filled.SkipNext, "Siguiente", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp)) }
            }

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidGlass(Modifier.weight(1f).clickable(onClick = onLyrics), RoundedCornerShape(16.dp), true) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MusicNote, null, tint = accent, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp)); Text("Letras", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                    }
                }
                LiquidGlass(Modifier.weight(1f).clickable(onClick = onAddPlaylist), RoundedCornerShape(16.dp), true) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp)); Text("Guardar", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (parsed.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(parsed.getOrNull(active)?.text ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 13.sp, color = accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private val SPECIAL_WORDS = setOf(
    "love", "amor", "corazón", "heart", "night", "noche", "baby", "vida",
    "fire", "luz", "dream", "sueño", "kiss", "beso", "forever", "siempre"
)

@Composable
fun KaraokePreview(lyrics: String, position: Long, accent: Color) {
    val lines = remember(lyrics) { parseLrc(lyrics) }
    val active = lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    Column {
        lines.drop(active).take(4).forEachIndexed { i, line ->
            val isActive = i == 0
            LyricsLineText(line.text, isActive, accent)
        }
    }
}

@Composable
fun LyricsFullscreen(
    song: Song,
    lyrics: String?,
    position: Long,
    accent: Color,
    onClose: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.background == DarkBg
    val lines = remember(lyrics) { parseLrc(lyrics ?: "") }
    val active = lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(active, lines.size) {
        if (lines.isNotEmpty() && active >= 0) runCatching { listState.animateScrollToItem(active, scrollOffset = -220) }
    }
    Box(Modifier.fillMaxSize().zIndex(30f)) {
        AsyncImage(song.coverUrl, null, ContentScale.Crop, Modifier.fillMaxSize().blur(70.dp))
        Box(Modifier.fillMaxSize().background(if (dark) Color.Black.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.80f)))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(onClick = onClose) { Icon(Icons.Filled.Close, "Cerrar", tint = MaterialTheme.colorScheme.onBackground) }
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text("LETRAS", fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f))
                    Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            if (lines.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay letras sincronizadas", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)) }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 100.dp, bottom = 220.dp), horizontalAlignment = Alignment.Start) {
                    items(lines.size) { i ->
                        val line = lines[i]
                        LyricsLineText(line.text, i == active, accent, big = true)
                        Spacer(Modifier.height(if (i == active) 22.dp else 14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsLineText(text: String, active: Boolean, accent: Color, big: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    val scale by animateFloatAsState(if (active) 1.015f else 1f, tween(250), label = "lyricScale")
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale },
        fontSize = when { big && active -> 28.sp; big -> 21.sp; active -> 19.sp; else -> 16.sp },
        lineHeight = when { big && active -> 34.sp; big -> 27.sp; else -> 23.sp },
        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
        color = if (active) cs.onBackground else cs.onBackground.copy(alpha = 0.38f),
        textAlign = TextAlign.Start
    )
}

/* ─── utils ─── */

data class LrcLine(val timeMs: Long, val text: String)

fun parseLrc(raw: String): List<LrcLine> {
    val re = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]\s*(.*)""")
    return raw.lines().mapNotNull { line ->
        val m = re.find(line.trim()) ?: return@mapNotNull null
        val min = m.groupValues[1].toLong()
        val sec = m.groupValues[2].toLong()
        val ms = m.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
        val text = m.groupValues[4].trim()
        if (text.isEmpty()) null else LrcLine(min * 60_000 + sec * 1000 + ms, text)
    }.sortedBy { it.timeMs }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val s = ms / 1000
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}
