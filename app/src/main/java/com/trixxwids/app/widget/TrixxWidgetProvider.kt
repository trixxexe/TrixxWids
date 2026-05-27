package com.trixxwids.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import com.trixxwids.app.R
import com.trixxwids.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrixxWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val prefs = context.getSharedPreferences("trixx_prefs", Context.MODE_PRIVATE)
        val savedWidgetId = prefs.getInt("pending_widget_id", -1)

        if (savedWidgetId == -1) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getDatabase(context).widgetDao()
            val widgetData = dao.getWidgetById(savedWidgetId)

            if (widgetData != null) {
                val bitmap = BitmapFactory.decodeFile(widgetData.previewImagePath)
                
                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_layout)
                        views.setImageViewBitmap(R.id.widget_image_view, bitmap)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
