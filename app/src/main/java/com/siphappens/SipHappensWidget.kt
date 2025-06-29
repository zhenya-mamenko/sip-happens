package com.siphappens

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class SipHappensWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_WIDGET_CLICK = "com.siphappens.ACTION_WIDGET_CLICK"
        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, SipHappensWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val counter = PreferenceManager.getCounter(context)
            val maximum = PreferenceManager.getMaximum(context)
            val imageCode = PreferenceManager.getImageCode(context)
            val imageResourceId = ImageHelper.getImageResourceId(imageCode, counter, maximum)
            views.setImageViewResource(R.id.widgetImage, imageResourceId)

            val intent = Intent(context, SipHappensWidget::class.java)
            intent.action = ACTION_WIDGET_CLICK
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widgetImage, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_CLICK) {
            val currentCounter = PreferenceManager.getCounter(context) + 1
            PreferenceManager.setCounter(context, currentCounter)
            val maximum = PreferenceManager.getMaximum(context)

            if (currentCounter >= maximum) {
                SoundHelper.playAfterMaxSound(context)
            } else {
                SoundHelper.playBeforeMaxSound(context)
            }

            updateWidget(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateWidget(context)
    }
}
