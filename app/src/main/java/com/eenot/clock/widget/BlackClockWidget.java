package com.eenot.clock.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.eenot.clock.R;
import com.eenot.clock.widget.config.BlackClockWidgetConfigActivity;

import java.util.Calendar;

public class BlackClockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateTime(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateTime(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.black_clock_widget);

        Calendar calendar = Calendar.getInstance();
        String time = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        views.setTextViewText(R.id.widget_time, time);

        // === ПРИМЕНЯЕМ НАСТРОЙКИ ===
        int alpha = WidgetPrefs.getInt(context, appWidgetId, "transparency", 255);
        int colorDrawable = WidgetPrefs.getInt(context, appWidgetId, "colorDrawable", R.drawable.widget_bg_black);

        // === НАСТРОЙКА КЛИКА ===
        Intent configIntent = new Intent(context, BlackClockWidgetConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
