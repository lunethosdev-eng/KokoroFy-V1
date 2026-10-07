@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.kokorofy.music

import androidx.compose.foundation.lazy.LazyListState

import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.material3.CircularProgressIndicator

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

private val SpotifyGreen = Color(0xFF1ED760)
private val Ink = Color(0xFF000000)
private val DarkSurface = Color(0xFF0A0A0C)
private val DarkSurface2 = Color(0xFF141416)
private val LightBg = Color(0xFFF5F5F7)
private val LightSurface = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: elimina los bordes blancos de status/nav bar
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val insets = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
        insets.isAppearanceLightStatusBars = false
        insets.isAppearanceLightNavigationBars = false
        setContent {
            var dark by rememberSaveable { mutableStateOf(true) }
            KokoroFyTheme(dark) { KokoroFyApp(dark = dark, onDarkChange = { dark = it }) }
        }
    }
}

@Composable
fun KokoroFyTheme(dark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (dark) {
        androidx.compose.material3.darkColorScheme(
            primary = SpotifyGreen,
            onPrimary = Color.Black,
            background = Color(0xFF000000),
            surface = DarkSurface,
            surfaceVariant = DarkSurface2,
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFFB8BCBE),
            outline = Color(0xFF34383C)
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF159447),
            onPrimary = Color.White,
            background = LightBg,
            surface = LightSurface,
            surfaceVariant = Color(0xFFF0F1EF),
            onSurface = Color(0xFF111315),
            onSurfaceVariant = Color(0xFF64686B),
            outline = Color(0xFFD5D8D5)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    interactive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    LiquidGlass(
        modifier = modifier,
        dark = dark,
        corner = RoundedCornerShape(22.dp),
        interactive = interactive
    ) {
        Column(
            Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun KokoroFyApp(
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { CatalogRepository(context) }
    val scope = rememberCoroutineScope()
    val songs by repo.songs.collectAsState(initial = emptyList())
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var current by remember { mutableStateOf<Song?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showEq by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var remoteSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var searchingRemote by remember { mutableStateOf(false) }
    val downloadProgress by OfflineManager.progress.collectAsState(initial = OfflineManager.Progress())
    var tab by remember { mutableIntStateOf(0) }
    var lyrics by remember { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var shuffle by remember { mutableStateOf(false) }
    var repeat by remember { mutableStateOf(false) }

    // Conectar MediaController SIN bloquear el hilo principal (evita ANR)
    DisposableEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            runCatching { controller = future.get() }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { future.cancel(true) }
            controller?.release()
            controller = null
        }
    }

    LaunchedEffect(Unit) {
        runCatching { repo.refresh() }
    }

    // Búsqueda remota con Seki: si no hay resultados locales, busca y descarga on-demand
    LaunchedEffect(query) {
        if (query.length < 2) {
            remoteSongs = emptyList()
            searchingRemote = false
            return@LaunchedEffect
        }
        delay(450) // debounce
        val localMatch = songs.any {
            "${it.title} ${it.artist} ${it.album}".contains(query, ignoreCase = true)
        }
        if (localMatch) {
            remoteSongs = emptyList()
            searchingRemote = false
            return@LaunchedEffect
        }
        searchingRemote = true
        // Seki funciona mejor con queries cortas; si pegan un link, pásalo tal cual
        val q = when {
            query.startsWith("http") -> query.trim()
            query.length > 48 -> query.take(48).trim()
            else -> query.trim()
        }
        val results = runCatching { SekiClient.search(q) }.getOrDefault(emptyList())
        remoteSongs = results
        searchingRemote = false
        // Auto-upsert a Room y auto-descarga del primero si hay resultados
        if (results.isNotEmpty()) {
            runCatching { repo.upsertAll(results) }
            // Iniciar descarga automática del primer resultado (el más relevante)
            OfflineManager.download(context, results.first())
        }
    }

    // Actualizar posición solo cuando reproduce (sin while agresivo)
    LaunchedEffect(controller, current?.id) {
        val c = controller ?: return@LaunchedEffect
        while (true) {
            val isPlaying = runCatching { c.isPlaying }.getOrDefault(false)
            if (playing != isPlaying) playing = isPlaying
            if (isPlaying) {
                position = runCatching { c.currentPosition.coerceAtLeast(0L) }.getOrDefault(0L)
                val d = runCatching { c.duration }.getOrDefault(0L)
                if (d > 0) duration = d
            }
            delay(if (isPlaying) 500 else 2000)
        }
    }

    fun play(song: Song) {
        current = song
        showPlayer = true
        showLyrics = false
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


    fun shareSong(song: Song) {
        val text = buildString {
            append("Escucha \"${song.title}\" de ${song.artist}")
            if (song.audioUrl.isNotBlank()) append("\n${song.audioUrl}")
            append("\n\n— vía KokoroFy")
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "${song.title} — ${song.artist}")
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Compartir canción"))
    }

    fun loadLyrics() {
        val song = current ?: return
        scope.launch {
            lyrics = LyricsRepository().fetch(song)?.let { it.synced ?: it.plain }
            showLyrics = true
        }
    }

    if (showSettings) {
        SettingsScreen(
            dark = dark,
            onDarkChange = onDarkChange,
            onBack = { showSettings = false },
            showEq = showEq,
            onEqChange = { showEq = it }
        )
        return
    }

    if (showPlayer && current != null) {
        FullPlayer(
            song = current!!,
            controller = controller,
            playing = playing,
            position = position,
            duration = duration,
            lyrics = lyrics,
            showLyrics = showLyrics,
            shuffle = shuffle,
            repeat = repeat,
            dark = dark,
            onClose = { showPlayer = false },
            onPlayPause = { if (playing) controller?.pause() else controller?.play() },
            onPrevious = { controller?.seekToPreviousMediaItem() },
            onNext = { controller?.seekToNextMediaItem() },
            onShuffle = {
                shuffle = !shuffle
                controller?.shuffleModeEnabled = shuffle
            },
            onRepeat = {
                repeat = !repeat
                controller?.repeatMode = if (repeat) 1 else 0
            },
            onLyrics = { if (lyrics == null) loadLyrics() else showLyrics = !showLyrics },
            onDownload = { OfflineManager.download(context, current!!) },
            onShare = { shareSong(current!!) },
            onSeek = { controller?.seekTo(it) }
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeTopBar(
                tab = tab,
                dark = dark,
                onSettings = { showSettings = true },
                onEq = { showEq = !showEq }
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = current != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    current?.let {
                        MiniPlayer(
                            song = it,
                            playing = playing,
                            dark = dark,
                            onOpen = { showPlayer = true },
                            onPlayPause = { if (playing) controller?.pause() else controller?.play() },
                            onNext = { controller?.seekToNextMediaItem() }
                        )
                    }
                }
                // Nav flotante Liquid Glass, rounded, separado del borde inferior
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    LiquidGlass(
                        modifier = Modifier.fillMaxWidth(),
                        dark = dark,
                        corner = RoundedCornerShape(28.dp),
                        interactive = false
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                Triple(0, Icons.Default.Home, "Inicio"),
                                Triple(1, Icons.Default.Search, "Buscar"),
                                Triple(2, Icons.Default.LibraryMusic, "Biblioteca")
                            ).forEach { (i, icon, name) ->
                                val selected = tab == i
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { tab = i }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = name,
                                        tint = if (selected) SpotifyGreen else Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        name,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) SpotifyGreen else Color.White.copy(alpha = 0.45f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize()
                .padding(pad)
                .padding(horizontal = 18.dp)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    songs = songs,
                    dark = dark,
                    showEq = showEq,
                    onPlay = ::play,
                    onDownload = { OfflineManager.download(context, it) }
                )
                1 -> SearchScreen(
                    songs = (songs.filter {
                        "${it.title} ${it.artist} ${it.album}".contains(query, true)
                    } + remoteSongs).distinctBy { it.id },
                    query = query,
                    dark = dark,
                    searching = searchingRemote,
                    onQuery = { query = it },
                    onPlay = ::play,
                    onDownload = { OfflineManager.download(context, it) }
                )
                else -> LibraryScreen(
                    songs = songs,
                    dark = dark,
                    onPlay = ::play,
                    onDownload = { OfflineManager.download(context, it) }
                )
            }
        }
    }

    if (showQueue) {
        QueueSheet(songs = songs, dark = dark, onPlay = ::play, onClose = { showQueue = false })
    }
    DownloadProgressOverlay(downloadProgress)
    } // end Box
}

@Composable
private fun HomeTopBar(tab: Int, dark: Boolean, onSettings: () -> Unit, onEq: () -> Unit) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = R.drawable.kokorofy_icon,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    when (tab) {
                        0 -> "KokoroFy"
                        1 -> "Buscar"
                        else -> "Tu biblioteca"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onEq) { Icon(Icons.Default.Equalizer, "Ecualizador") }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Ajustes") }
        }
    )
}

@Composable
private fun HomeScreen(
    songs: List<Song>,
    dark: Boolean,
    showEq: Boolean,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text("Buenas noches", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Tu música,\nexactamente como quieres.",
                fontSize = 32.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.2).sp
            )
        }
        if (showEq) item { EqualizerCard(dark) }
        if (songs.isNotEmpty()) {
            item {
                Text("Escuchado recientemente", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    songs.take(6).forEach { song ->
                        AlbumCard(song, dark, onPlay)
                    }
                }
            }
            item {
                Text("Para ti", fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
            items(songs.take(12), key = { it.id }) { song ->
                TrackRow(song, dark, onPlay, onDownload)
            }
        } else {
            item { EmptyState("Tu catálogo está esperando música.") }
        }
    }
}

@Composable
private fun AlbumCard(song: Song, dark: Boolean, onPlay: (Song) -> Unit) {
    Column(
        Modifier.width(145.dp).clickable { onPlay(song) }
    ) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(145.dp).clip(RoundedCornerShape(18.dp))
        )
        Spacer(Modifier.height(8.dp))
        Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun SearchScreen(
    songs: List<Song>,
    query: String,
    dark: Boolean,
    searching: Boolean = false,
    onQuery: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 14.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Artistas, canciones o álbumes") },
            shape = RoundedCornerShape(18.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SpotifyGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        if (searching) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SpotifyGreen,
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.width(12.dp))
                Text("Buscando y descargando en Seki…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(songs, key = { it.id }) { TrackRow(it, dark, onPlay, onDownload) }
            if (songs.isEmpty() && !searching && query.length >= 2) {
                item { EmptyState("No encontramos nada. Prueba un nombre más corto o un link.") }
            }
            if (songs.isEmpty() && query.isBlank()) {
                item { EmptyState("Busca una canción, artista o pega un link de YouTube.") }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    songs: List<Song>,
    dark: Boolean,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LibraryPill(Icons.Default.Download, "Descargas")
                LibraryPill(Icons.Default.FavoriteBorder, "Favoritos")
            }
        }
        item { Text("Canciones", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold) }
        items(songs, key = { it.id }) { TrackRow(it, dark, onPlay, onDownload) }
    }
}

@Composable
private fun RowScope.LibraryPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    GlassCard(Modifier.weight(1f), dark = MaterialTheme.colorScheme.background == Ink) {
        Icon(icon, null, tint = SpotifyGreen)
        Spacer(Modifier.height(8.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TrackRow(
    song: Song,
    dark: Boolean,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlay(song) }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${song.artist} • ${song.album}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = { onDownload(song) }) {
            Icon(Icons.Default.Download, "Descargar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MiniPlayer(
    song: Song,
    playing: Boolean,
    dark: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    LiquidGlass(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onOpen() },
        dark = dark,
        corner = RoundedCornerShape(20.dp),
        interactive = true
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl ?: R.drawable.kokorofy_icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (dark) Color.White else Color.Black
                )
                Text(
                    song.artist,
                    fontSize = 12.sp,
                    color = if (dark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (dark) Color.White else Color.Black
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = if (dark) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
private fun FullPlayer(
    song: Song,
    controller: MediaController?,
    playing: Boolean,
    position: Long,
    duration: Long,
    lyrics: String?,
    showLyrics: Boolean,
    shuffle: Boolean,
    repeat: Boolean,
    dark: Boolean,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onLyrics: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val parsed = remember(lyrics) { parseLyrics(lyrics) }
    val activeLine = remember(parsed, position) {
        parsed.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    }
    val lyricState = rememberLazyListState()

    LaunchedEffect(activeLine, showLyrics) {
        if (showLyrics && parsed.isNotEmpty()) {
            lyricState.animateScrollToItem(max(0, activeLine - 3))
        }
    }

    Box(Modifier.fillMaxSize().background(if (dark) Color.Black else Color(0xFFF2F2F7))) {
        // Portada de fondo (estilo Spotify) — visible y con scrim oscuro/claro
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(if (dark) 0.45f else 0.28f)
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    if (dark) listOf(
                        Color.Black.copy(alpha = 0.55f),
                        Color.Black.copy(alpha = 0.75f),
                        Color.Black.copy(alpha = 0.92f)
                    ) else listOf(
                        Color.White.copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.95f)
                    )
                )
            )
        )

        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, "Cerrar") }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPRODUCIENDO DESDE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(song.album.ifBlank { "KokoroFy" }, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onShare) { Icon(Icons.Default.Share, "Compartir") }
                IconButton(onClick = onDownload) { Icon(Icons.Default.Download, "Descargar") }
            }

            AnimatedVisibility(!showLyrics, Modifier.weight(1f)) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = song.coverUrl ?: R.drawable.kokorofy_icon,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                            .clip(RoundedCornerShape(25.dp))
                            .shadow(22.dp, RoundedCornerShape(25.dp))
                    )
                    Spacer(Modifier.height(22.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(song.title, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (dark) Color.White else Color.Black)
                            Text(song.artist, fontSize = 16.sp, color = if (dark) Color.White.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.55f))
                        }
                        IconButton(onClick = {}) { Icon(Icons.Default.FavoriteBorder, "Me gusta") }
                    }
                    Spacer(Modifier.height(12.dp))
                    var scrubbing by remember { mutableStateOf(false) }
                    var scrubPos by remember { mutableFloatStateOf(0f) }
                    val sliderValue = if (scrubbing) scrubPos else (position.toFloat() / duration.coerceAtLeast(1L)).coerceIn(0f, 1f)
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            scrubbing = true
                            scrubPos = it
                        },
                        onValueChangeFinished = {
                            onSeek((scrubPos * duration).toLong())
                            scrubbing = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = SpotifyGreen,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = if (dark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.15f)
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(position), fontSize = 11.sp, color = if (dark) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.45f))
                        Text(formatTime(duration), fontSize = 11.sp, color = if (dark) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.45f))
                    }
                }
            }

            AnimatedVisibility(showLyrics, Modifier.weight(1f)) {
                if (parsed.isNotEmpty()) {
                    LazyColumn(
                        state = lyricState,
                        contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp, start = 4.dp, end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(parsed) { index, line ->
                            val isActive = index == activeLine
                            val dist = kotlin.math.abs(index - activeLine)
                            Text(
                                line.text,
                                fontSize = when {
                                    isActive -> 26.sp
                                    dist == 1 -> 18.sp
                                    else -> 16.sp
                                },
                                lineHeight = when {
                                    isActive -> 34.sp
                                    else -> 24.sp
                                },
                                fontWeight = when {
                                    isActive -> FontWeight.Bold
                                    dist <= 1 -> FontWeight.SemiBold
                                    else -> FontWeight.Normal
                                },
                                color = when {
                                    isActive -> SpotifyGreen
                                    dist == 1 -> if (dark) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.70f)
                                    dist == 2 -> if (dark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.40f)
                                    else -> if (dark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.22f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable { onSeek(line.timeMs) }
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lyrics, null, Modifier.size(50.dp), tint = SpotifyGreen)
                            Spacer(Modifier.height(12.dp))
                            Text(lyrics ?: "Cargando letras…", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(Icons.Default.Shuffle, "Aleatorio", tint = if (shuffle) SpotifyGreen else MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, "Anterior", Modifier.size(32.dp)) }
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(66.dp),
                    shape = CircleShape
                ) {
                    Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Reproducir", Modifier.size(32.dp))
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Siguiente", Modifier.size(32.dp)) }
                IconButton(onClick = onRepeat) {
                    Icon(Icons.Default.Repeat, "Repetir", tint = if (repeat) SpotifyGreen else MaterialTheme.colorScheme.onSurface)
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "Ecualizador",
                    modifier = Modifier.clickable { },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (showLyrics) "Portada" else "Letras",
                    modifier = Modifier.clickable(onClick = onLyrics),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showLyrics) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Cola",
                    modifier = Modifier.clickable { controller?.seekToNextMediaItem() },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


private fun parseLyrics(raw: String?): List<LyricLine> {
    if (raw.isNullOrBlank()) return emptyList()
    val result = mutableListOf<LyricLine>()
    raw.lines().forEach { line ->
        val matches = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?\]""").findAll(line).toList()
        val text = line.replace(Regex("""\[\d{1,3}:\d{2}(?:[.:]\d{1,3})?\]"""), "").trim()
        matches.forEach { m ->
            val minutes = m.groupValues[1].toLong()
            val seconds = m.groupValues[2].toLong()
            val fraction = m.groupValues.getOrNull(3)?.toLongOrNull() ?: 0L
            val millis = if (fraction < 100) fraction * 10 else fraction
            result += LyricLine(minutes * 60_000 + seconds * 1_000 + millis, text)
        }
    }
    return if (result.isEmpty()) raw.lines().filter { it.isNotBlank() }.mapIndexed { i, t -> LyricLine(i * 4000L, t) }
    else result.sortedBy { it.timeMs }
}

@Composable
private fun SettingsScreen(
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    showEq: Boolean,
    onEqChange: (Boolean) -> Unit
) {
    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Ajustes", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection("Apariencia") {
                    SettingRow(Icons.Default.DarkMode, "Modo oscuro", "Interfaz nocturna de alto contraste") {
                        Switch(checked = dark, onCheckedChange = onDarkChange)
                    }
                    SettingRow(Icons.Default.NightsStay, "Liquid glass", "Superficies translúcidas con profundidad") {
                        Text("ACTIVO", color = SpotifyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                SettingsSection("Reproducción") {
                    SettingRow(Icons.Default.Equalizer, "Ecualizador", "Visualizador y controles de audio") {
                        Switch(checked = showEq, onCheckedChange = onEqChange)
                    }
                    SettingRow(Icons.Default.Speed, "Calidad", "Audio adaptado al archivo original") {
                        Text("AUTO", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    SettingRow(Icons.Default.QueueMusic, "Cola de reproducción", "Gestiona lo que sigue") {
                        Icon(Icons.Default.MoreVert, null)
                    }
                }
            }
            item {
                SettingsSection("KokoroFy") {
                    SettingRow(Icons.Default.Info, "Versión", "1.4.1") {}
                    SettingRow(Icons.Default.Bolt, "Motor", "Media3 / ExoPlayer") {}
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        GlassCard(Modifier.fillMaxWidth(), dark = MaterialTheme.colorScheme.background == Ink, content = content)
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
private fun EqualizerCard(dark: Boolean) {
    GlassCard(Modifier.fillMaxWidth(), dark = dark) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Equalizer, null, tint = SpotifyGreen)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Visualizador", fontWeight = FontWeight.Bold)
                Text("KokoroFy está listo para sonar.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(14.dp))
        val transition = rememberInfiniteTransition(label = "bars")
        Row(
            Modifier.fillMaxWidth().height(55.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(24) { i ->
                val h by transition.animateFloat(
                    initialValue = (12 + (i % 5) * 5).toFloat(),
                    targetValue = (24 + ((i * 7) % 27)).toFloat(),
                    animationSpec = infiniteRepeatable(tween(350 + i * 12, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "bar$i"
                )
                Box(
                    Modifier.weight(1f).height(h.dp).clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(SpotifyGreen.copy(alpha = .9f), SpotifyGreen.copy(alpha = .18f))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun QueueSheet(
    songs: List<Song>,
    dark: Boolean,
    onPlay: (Song) -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            Text("Cola", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            songs.take(15).forEach {
                TrackRow(it, dark, onPlay, {})
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    GlassCard(Modifier.fillMaxWidth(), dark = MaterialTheme.colorScheme.background == Ink) {
        Icon(Icons.Default.MusicNote, null, tint = SpotifyGreen, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(10.dp))
        Text(text, fontWeight = FontWeight.Bold)
        Text("Cuando agregues canciones aparecerán aquí.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

fun formatTime(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}
