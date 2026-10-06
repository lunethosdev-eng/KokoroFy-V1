package com.kokorofy.music

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Widget sincronizado: la app llama PlayerWidget.updateAll() al cambiar pista/play.
 */
class PlayerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> apply(context, manager, id, state) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SYNC) {
            state = WidgetState(
                title = intent.getStringExtra(EXTRA_TITLE) ?: state.title,
                artist = intent.getStringExtra(EXTRA_ARTIST) ?: state.artist,
                playing = intent.getBooleanExtra(EXTRA_PLAYING, state.playing)
            )
            updateAll(context)
        }
    }

    companion object {
        const val ACTION_SYNC = "com.kokorofy.music.WIDGET_SYNC"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
        const val EXTRA_PLAYING = "playing"

        @Volatile
        var state = WidgetState("KokoroFy", "Toca una canción", false)

        data class WidgetState(val title: String, val artist: String, val playing: Boolean)

        fun sync(context: Context, title: String, artist: String, playing: Boolean) {
            state = WidgetState(title, artist, playing)
            val i = Intent(ACTION_SYNC).setPackage(context.packageName)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_ARTIST, artist)
                .putExtra(EXTRA_PLAYING, playing)
            context.sendBroadcast(i)
            updateAll(context)
        }

        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, PlayerWidget::class.java))
            ids.forEach { apply(context, mgr, it, state) }
        }

        private fun apply(
            context: Context,
            manager: AppWidgetManager,
            id: Int,
            s: WidgetState
        ) {
            val views = RemoteViews(context.packageName, R.layout.player_widget)
            views.setTextViewText(R.id.widgetTitle, s.title)
            views.setTextViewText(R.id.widgetArtist, s.artist)
            views.setTextViewText(
                R.id.widgetPlay,
                if (s.playing) "❚❚" else "▶"
            )
            val launch = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, launch)
            views.setOnClickPendingIntent(R.id.widgetPlay, launch)
            manager.updateAppWidget(id, views)
        }
    }
}
