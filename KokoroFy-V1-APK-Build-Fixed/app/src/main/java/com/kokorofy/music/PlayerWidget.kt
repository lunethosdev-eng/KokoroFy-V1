package com.kokorofy.music

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PlayerWidget: AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray){
        ids.forEach{ id ->
            val views=RemoteViews(context.packageName,R.layout.player_widget)
            val launch=PendingIntent.getActivity(
                context,0,Intent(context,MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetPlay,launch)
            manager.updateAppWidget(id,views)
        }
    }
}
