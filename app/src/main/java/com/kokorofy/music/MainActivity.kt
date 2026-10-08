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
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
            var dark by rememberSaveable { mutableStateOf(Prefs.isDark(this@MainActivity)) }
            SideEffect {
                insets.isAppearanceLightStatusBars = !dark
                insets.isAppearanceLightNavigationBars = !dark
                val secure = Prefs.blockScreenshots(this@MainActivity) || FeaturePrefs.get(this@MainActivity, "privacy.screenshots")
                if (secure) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
            KokoroFyTheme(dark) {
                KokoroFyApp(
                    dark = dark,
                    onDarkChange = {
                        dark = it
                        Prefs.setDark(this@MainActivity, it)
                    }
                )
            }
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
    var showEq by remember { mutableStateOf(Prefs.showEq(context)) }
    var showAccount by remember { mutableStateOf(false) }
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
        if (FeaturePrefs.get(context, "privacy.local_only") || FeaturePrefs.get(context, "privacy.redact_search")) {
            remoteSongs = emptyList()
            searchingRemote = false
            return@LaunchedEffect
        }
        delay(450) // debounce
        searchingRemote = true

        // Seki recibe el texto completo. Si el usuario pega un enlace de YouTube,
        // no lo recortamos: el backend puede resolverlo directamente.
        val q = query.trim()
        val results = runCatching { SekiClient.search(q) }.getOrDefault(emptyList())

        remoteSongs = results
        searchingRemote = false

        // Guarda los resultados en Room para que aparezcan en la biblioteca
        // después de la búsqueda. La reproducción usa el audio_url devuelto por Seki.
        if (results.isNotEmpty()) {
            runCatching { repo.upsertAll(results) }
            if (FeaturePrefs.get(context, "extra.auto_download", true) &&
                results.size == 1 &&
                q.startsWith("http", ignoreCase = true)
            ) {
                OfflineManager.download(context, results.first())
            }
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
        TasteManager.recordPlay(context, song.id)
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
                            .setArtworkUri(
                                if (Prefs.hideNotificationArt(context) || FeaturePrefs.get(context, "privacy.notification_art"))
                                    null
                                else song.coverUrl?.let(android.net.Uri::parse)
                            )
                            .build()
                    ).build()
            )
            prepare()
            play()
        }
        if (Prefs.autoLoadLyrics(context) || FeaturePrefs.get(context, "extra.auto_lyrics", true)) {
            scope.launch {
                lyrics = LyricsRepository().fetch(song)?.let { it.synced ?: it.plain }
            }
        }
    }


    fun shareSong(song: Song) {
        if (FeaturePrefs.get(context, "privacy.external_share")) return
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

    val activity = context as? ComponentActivity
    val keepScreen = Prefs.keepScreenOn(context) || FeaturePrefs.get(context, "ui.keep_screen")
    DisposableEffect(keepScreen, Prefs.blockScreenshots(context), FeaturePrefs.get(context, "privacy.screenshots")) {
        val window = activity?.window
        if (keepScreen) window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val secure = Prefs.blockScreenshots(context) || FeaturePrefs.get(context, "privacy.screenshots")
        if (secure) window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        else window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { }
    }

    if (showSettings) {
        SettingsScreen(
            context = context,
            dark = dark,
            onDarkChange = onDarkChange,
            onBack = { showSettings = false },
            showEq = showEq,
            onEqChange = { showEq = it; Prefs.setShowEq(context, it) },
            onAccount = { showSettings = false; showAccount = true }
        )
    } else {
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
                            position = position,
                            duration = duration,
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
                         .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
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
                                .height(if (FeaturePrefs.get(context, "ui.nav_compact")) 54.dp else 62.dp)
                                .padding(horizontal = 6.dp),
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
                                        tint = if (selected) SpotifyGreen else if (dark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.45f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        name,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) SpotifyGreen else if (dark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.45f)
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

    AnimatedVisibility(
        visible = showPlayer && current != null,
        enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { it / 6 }, animationSpec = tween(260)),
        exit = fadeOut(tween(170)) + slideOutVertically(targetOffsetY = { it / 8 }, animationSpec = tween(240))
    ) {
        current?.let { song ->
            FullPlayer(
                song = song,
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
                onDownload = { OfflineManager.download(context, song) },
                onShare = { shareSong(song) },
                onFavorite = { TasteManager.toggleFavorite(context, song.id) },
                onSeek = { controller?.seekTo(it) }
            )
        }
    }

    if (showAccount) {
        AccountSheet(context = context, onClose = { showAccount = false })
    }
    } // end Box
    }
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
        LiquidGlass(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 14.dp),
            dark = dark,
            corner = RoundedCornerShape(20.dp),
            interactive = true,
            intensity = 0.95f
        ) {
            Row(
                Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(10.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQuery,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    decorationBox = { inner ->
                        if (query.isBlank()) {
                            Text(
                                "Canciones, artistas o pega un link de YouTube",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f),
                                fontSize = 15.sp
                            )
                        }
                        inner()
                    }
                )
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQuery("") }) {
                        Icon(Icons.Default.Close, "Borrar búsqueda")
                    }
                }
            }
        }
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
                Text("Buscando en Seki…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
    LiquidGlass(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        dark = dark,
        corner = RoundedCornerShape(18.dp),
        interactive = true,
        intensity = 0.62f
    ) {
    Row(
        Modifier.fillMaxWidth()
            .clickable { onPlay(song) }
            .padding(horizontal = 8.dp, vertical = 7.dp),
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
}

@Composable
private fun MiniPlayer(
    song: Song,
    playing: Boolean,
    position: Long,
    duration: Long,
    dark: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    LiquidGlass(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        dark = dark,
        corner = RoundedCornerShape(22.dp),
        interactive = true
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clickable { onOpen() }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl ?: R.drawable.kokorofy_icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp))
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
                    color = if (dark) Color.White.copy(alpha = 0.68f) else Color.Black.copy(alpha = 0.60f),
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
        if (FeaturePrefs.get(LocalContext.current, "ui.mini_progress", true)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (dark) Color.White.copy(.10f) else Color.Black.copy(.10f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((position.toFloat() / duration.coerceAtLeast(1L)).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(SpotifyGreen)
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
    onFavorite: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val fullPlayerContext = LocalContext.current
    val parsed = remember(lyrics) { parseLyrics(lyrics) }
    val activeLine = remember(parsed, position) {
        parsed.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    }
    val lyricState = rememberLazyListState()
    val tilt = rememberDeviceTilt(
        Prefs.gyro(LocalContext.current) || FeaturePrefs.get(LocalContext.current, "ui.gyro_3d", true)
    )
    val smoothTiltX by androidx.compose.animation.core.animateFloatAsState(
        targetValue = tilt.x,
        animationSpec = androidx.compose.animation.core.spring(stiffness = 170f, dampingRatio = 0.82f),
        label = "cover-tilt-x"
    )
    val smoothTiltY by androidx.compose.animation.core.animateFloatAsState(
        targetValue = tilt.y,
        animationSpec = androidx.compose.animation.core.spring(stiffness = 170f, dampingRatio = 0.82f),
        label = "cover-tilt-y"
    )

    LaunchedEffect(activeLine, showLyrics) {
        if (showLyrics && parsed.isNotEmpty()) {
            lyricState.animateScrollToItem(max(0, activeLine - 3))
        }
    }

    val isDark = dark
    Box(Modifier.fillMaxSize().background(if (isDark) Color.Black else LightBg)) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.kokorofy_icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(if (FeaturePrefs.get(LocalContext.current, "ui.blur", true)) 30.dp else 0.dp)
                .alpha(if (isDark) 0.42f else 0.22f)
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    if (isDark) listOf(
                        Color.Black.copy(alpha = 0.50f),
                        Color.Black.copy(alpha = 0.74f),
                        Color.Black.copy(alpha = 0.95f)
                    ) else listOf(
                        Color.White.copy(alpha = 0.68f),
                        Color.White.copy(alpha = 0.84f),
                        Color.White.copy(alpha = 0.96f)
                    )
                )
            )
        )

        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidGlass(
                    modifier = Modifier.size(42.dp),
                    dark = isDark,
                    corner = RoundedCornerShape(21.dp),
                    interactive = true
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Minimizar",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "REPRODUCIENDO DESDE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(.65f) else Color.Black.copy(.55f)
                    )
                    Text(song.album.ifBlank { "KokoroFy" }, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, "Compartir", tint = if (isDark) Color.White else Color.Black)
                    }
                    IconButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, "Descargar", tint = if (isDark) Color.White else Color.Black)
                    }
                }
            }

            AnimatedVisibility(
                visible = !showLyrics,
                modifier = Modifier.weight(1f),
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(140))
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = song.coverUrl ?: R.drawable.kokorofy_icon,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .graphicsLayer {
                                rotationY = if (FeaturePrefs.get(fullPlayerContext, "ui.cover_parallax", true)) smoothTiltY * 0.72f else 0f
                                rotationX = if (FeaturePrefs.get(fullPlayerContext, "ui.cover_parallax", true)) -smoothTiltX * 0.58f else 0f
                                cameraDistance = 24f * density
                                translationX = smoothTiltY * 0.9f
                                translationY = smoothTiltX * 0.55f
                                scaleX = 1f + kotlin.math.abs(smoothTiltY) * 0.0014f
                                scaleY = 1f + kotlin.math.abs(smoothTiltX) * 0.0014f
                            }
                            .clip(RoundedCornerShape(28.dp))
                            .shadow(28.dp, RoundedCornerShape(28.dp))
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                song.title,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Text(
                                song.artist,
                                fontSize = 16.sp,
                                color = if (isDark) Color.White.copy(.65f) else Color.Black.copy(.55f)
                            )
                        }
                        val favorite = TasteManager.isFavorite(LocalContext.current, song.id)
                        IconButton(onClick = onFavorite) {
                            Icon(
                                if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                "Me gusta",
                                tint = if (favorite) SpotifyGreen else if (isDark) Color.White else Color.Black
                            )
                        }
                    }

                    var scrubbing by remember { mutableStateOf(false) }
                    var scrubPos by remember { mutableFloatStateOf(0f) }
                    val sliderValue = if (scrubbing) scrubPos
                    else (position.toFloat() / duration.coerceAtLeast(1L)).coerceIn(0f, 1f)
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
                            inactiveTrackColor = if (isDark) Color.White.copy(.20f) else Color.Black.copy(.15f)
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(position), fontSize = 11.sp, color = if (isDark) Color.White.copy(.55f) else Color.Black.copy(.45f))
                        Text(formatTime(duration), fontSize = 11.sp, color = if (isDark) Color.White.copy(.55f) else Color.Black.copy(.45f))
                    }
                }
            }

            AnimatedVisibility(
                visible = showLyrics,
                modifier = Modifier.weight(1f),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(140))
            ) {
                if (parsed.isNotEmpty()) {
                    LazyColumn(
                        state = lyricState,
                        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 2.dp, end = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(parsed) { index, line ->
                            val isActive = index == activeLine
                            val nextTime = parsed.getOrNull(index + 1)?.timeMs ?: (line.timeMs + 2600L)
                            LyricLineAnimated(
                                line = line,
                                position = position,
                                nextTime = nextTime,
                                active = isActive,
                                dark = isDark,
                                onSeek = onSeek
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lyrics, null, Modifier.size(50.dp), tint = SpotifyGreen)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                lyrics ?: "Cargando letras…",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(Icons.Default.Shuffle, "Aleatorio", tint = if (shuffle) SpotifyGreen else MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, "Anterior", Modifier.size(32.dp)) }
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(68.dp),
                    shape = CircleShape
                ) {
                    Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Reproducir", Modifier.size(33.dp))
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Siguiente", Modifier.size(32.dp)) }
                IconButton(onClick = onRepeat) {
                    Icon(Icons.Default.Repeat, "Repetir", tint = if (repeat) SpotifyGreen else MaterialTheme.colorScheme.onSurface)
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(bottom = 7.dp),
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
                    "Siguiente",
                    modifier = Modifier.clickable { controller?.seekToNextMediaItem() },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LyricLineAnimated(
    line: LyricLine,
    position: Long,
    nextTime: Long,
    active: Boolean,
    dark: Boolean,
    onSeek: (Long) -> Unit
) {
    val duration = (nextTime - line.timeMs).coerceIn(700L, 8000L)

    // Karaoke 1.3x: the highlight advances a little faster than the raw
    // line progress while the spring keeps the visual movement smooth.
    val rawProgress = ((position - line.timeMs).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    val targetProgress = if (active) (rawProgress * 1.30f).coerceIn(0f, 1f)
    else if (position < line.timeMs) 0f else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(if (active) 65 else 180, easing = FastOutSlowInEasing),
        label = "lyric-progress-1-3x"
    )

    val base = if (dark) Color.White.copy(if (active) .70f else .25f)
    else Color.Black.copy(if (active) .70f else .25f)
    val textSize = if (active) 26.sp else 17.sp
    val charsToShow = (line.text.length * animatedProgress).toInt().coerceIn(0, line.text.length)

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSeek(line.timeMs) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            buildAnnotatedString {
                var cursor = 0
                val words = Regex("""\\S+|\\s+""").findAll(line.text)
                words.forEach { match ->
                    val word = match.value
                    val emotion = lyricWordEmotion(word)
                    val (font, color) = when (emotion) {
                        "love" -> FontFamily.Serif to Color(0xFFFF78B7)
                        "sad" -> FontFamily.Cursive to Color(0xFF78AEFF)
                        "anger" -> FontFamily.Monospace to Color(0xFFFF675B)
                        "dream" -> FontFamily.Serif to Color(0xFFB89BFF)
                        "energy" -> FontFamily.Default to Color(0xFFFFD166)
                        else -> FontFamily.Default to if (dark) Color.White else Color.Black
                    }

                    word.forEachIndexed { offset, char ->
                        val index = cursor + offset
                        val revealed = index < charsToShow
                        val alpha = when {
                            revealed && active -> 1f
                            active -> .52f
                            position >= nextTime -> .28f
                            else -> .62f
                        }
                        append(char)
                        addStyle(
                            SpanStyle(
                                color = if (revealed && active) color.copy(alpha = alpha)
                                else base.copy(alpha = alpha),
                                fontFamily = font,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                            ),
                            start = length - 1,
                            end = length
                        )
                    }
                    cursor += word.length
                }
            },
            fontSize = textSize,
            lineHeight = if (active) 34.sp else 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun lyricWordEmotion(word: String): String {
    val s = word.lowercase()
    return when {
        listOf("love", "amor", "heart", "kiss", "babe", "baby", "tequiero", "beso", "kissed").any(s::contains) -> "love"
        listOf("sad", "lloro", "llorar", "alone", "lonely", "triste", "lágrima", "cry", "hurt").any(s::contains) -> "sad"
        listOf("hate", "angry", "rage", "odio", "fuego", "kill", "enoj", "damn").any(s::contains) -> "anger"
        listOf("dream", "dreaming", "sueño", "noche", "star", "cielo", "moon", "night").any(s::contains) -> "dream"
        listOf("yeah", "hey", "run", "dance", "fire", "go").any(s::contains) -> "energy"
        else -> "neutral"
    }
}

private fun lyricEmotion(text: String): String = lyricWordEmotion(text)

private fun parseLyrics(raw: String?): List<LyricLine> {
    if (raw.isNullOrBlank()) return emptyList()
    val result = mutableListOf<LyricLine>()
    raw.lines().forEach { line ->
        val matches = Regex("""\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?\\]""").findAll(line).toList()
        val text = line.replace(Regex("""\\[\\d{1,3}:\\d{2}(?:[.:]\\d{1,3})?\\]"""), "").trim()
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
    context: android.content.Context,
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    showEq: Boolean,
    onEqChange: (Boolean) -> Unit,
    onAccount: () -> Unit
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
                SettingsSection("Cuenta") {
                    val auth = remember { AuthRepository(context) }
                    val state = remember { auth.state() }
                    SettingRow(Icons.Default.Person, "Cuenta KokoroFy",
                        state.email ?: "Regístrate para conservar tu sesión") {
                        OutlinedButton(onClick = onAccount) { Text(if (state.email == null) "Entrar" else "Cuenta") }
                    }
                }
            }
            item {
                SettingsSection("Apariencia") {
                    SettingRow(Icons.Default.DarkMode, "Modo oscuro", "Interfaz nocturna de alto contraste") {
                        Switch(checked = dark, onCheckedChange = onDarkChange)
                    }
                    SettingRow(Icons.Default.NightsStay, "Liquid Glass", "Cristal translúcido con specular y deformación") {
                        FeatureSwitch(context, "ui.glass", true)
                    }
                    SettingRow(Icons.Default.Bolt, "Giroscopio 3D", "Inclina la portada del full player con el teléfono") {
                        FeatureSwitch(context, "ui.gyro_3d", true)
                    }
                    SettingRow(Icons.Default.Lyrics, "Karaoke por carácter", "Colorea letra por letra durante la reproducción") {
                        FeatureSwitch(context, "ui.lyrics_karaoke", true)
                    }
                    SettingRow(Icons.Default.Equalizer, "Ecualizador", "Visualizador animado") {
                        Switch(checked = showEq, onCheckedChange = onEqChange)
                    }
                }
            }
            item {
                SettingsSection("Privacidad · 20 controles") {
                    FeaturePrefs.privacy.forEach { (key, title, subtitle) ->
                        SettingRow(Icons.Default.Info, title, subtitle) {
                            FeatureSwitch(context, key)
                        }
                    }
                }
            }
            item {
                SettingsSection("Personalización · 30 controles") {
                    FeaturePrefs.customization.forEach { (key, title, subtitle) ->
                        SettingRow(Icons.Default.Settings, title, subtitle) {
                            FeatureSwitch(context, key)
                        }
                    }
                }
            }
            item {
                SettingsSection("Experimental · 20 controles") {
                    FeaturePrefs.experimental.forEach { (key, title, subtitle) ->
                        SettingRow(Icons.Default.Bolt, title, subtitle) {
                            FeatureSwitch(context, key)
                        }
                    }
                }
            }
            item {
                SettingsSection("Más funciones · 60 controles") {
                    FeaturePrefs.extra.forEach { (key, title, subtitle) ->
                        SettingRow(Icons.Default.Cached, title, subtitle) {
                            FeatureSwitch(context, key)
                        }
                    }
                }
            }
            item {
                SettingsSection("Reproducción") {
                    SettingRow(Icons.Default.Speed, "Velocidad", "Velocidad de reproducción persistente") {
                        Text("${Prefs.speed(context)}x", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    SettingRow(Icons.Default.QueueMusic, "Gapless", "Evita pausas entre pistas compatibles") {
                        Switch(
                            checked = Prefs.gapless(context) || FeaturePrefs.get(context, "extra.gapless"),
                            onCheckedChange = { Prefs.setGapless(context, it); FeaturePrefs.set(context, "extra.gapless", it) }
                        )
                    }
                    SettingRow(Icons.Default.Info, "Versión", "1.5.0 · Liquid Glass build") {}
                    SettingRow(Icons.Default.Bolt, "Motor", "Media3 / ExoPlayer") {}
                }
            }
        }
    }
}

@Composable
private fun FeatureSwitch(context: android.content.Context, key: String, default: Boolean = false) {
    var checked by remember(key) { mutableStateOf(FeaturePrefs.get(context, key, default)) }
    Switch(
        checked = checked,
        onCheckedChange = {
            checked = it
            FeaturePrefs.set(context, key, it)
        }
    )
}

@Composable
private fun AccountSheet(context: android.content.Context, onClose: () -> Unit) {
    val repo = remember { AuthRepository(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signUp by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var loggedIn by remember { mutableStateOf(repo.state().email != null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onClose) {
        LiquidGlass(
            modifier = Modifier.fillMaxWidth(),
            dark = MaterialTheme.colorScheme.background == Ink,
            corner = RoundedCornerShape(28.dp),
            intensity = 0.95f
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (loggedIn) "Tu cuenta" else if (signUp) "Crear cuenta" else "Iniciar sesión",
                        fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") }
                }
                if (loggedIn) {
                    Text(repo.state().email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(18.dp))
                    Text("Tus playlists, historial local y preferencias siguen guardados en este dispositivo.")
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true
                                repo.signOut()
                                loggedIn = false
                                busy = false
                            }
                        },
                        enabled = !busy
                    ) { Text("Sign out") }
                } else {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Correo") }
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Contraseña") }
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true
                                message = null
                                val result = if (signUp) repo.signUp(email, password) else repo.signIn(email, password)
                                result.onSuccess {
                                    loggedIn = true
                                    message = if (signUp) "Cuenta creada. Si Supabase pide confirmación, revisa tu correo." else "Sesión iniciada."
                                }.onFailure { message = it.message }
                                busy = false
                            }
                        },
                        enabled = !busy && email.contains("@") && password.length >= 6,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (busy) "Procesando…" else if (signUp) "Registrarme" else "Iniciar sesión") }
                    TextButton(onClick = { signUp = !signUp; message = null }) {
                        Text(if (signUp) "Ya tengo cuenta" else "Crear una cuenta")
                    }
                    message?.let {
                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
