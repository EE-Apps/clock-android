package com.eenot.clock.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.eenot.clock.R;
import com.eenot.clock.widget.config.BlackDateWidgetConfigActivity;

import java.util.Calendar;
import java.util.Locale;

public class BlackDateWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateDate(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateDate(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.black_date_widget);

        Calendar calendar = Calendar.getInstance();

        // Получаем число месяца
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.format("%02d", day); // например, "05"

        // Верхняя и нижняя цифра
        views.setTextViewText(R.id.day_digit_top, String.format("%02d", day));

        // Аббревиатура месяца (3 буквы, капсом)
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        if (month != null) month = month.toUpperCase(Locale.ROOT);
        views.setTextViewText(R.id.month_abbr, month);

        // День недели с заглавной буквы полностью
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        if (dayOfWeek != null) dayOfWeek = dayOfWeek.substring(0,1).toUpperCase() + dayOfWeek.substring(1);
        views.setTextViewText(R.id.day_of_week, dayOfWeek);
        
        // Получаем и применяем прозрачность к фону (ImageView)
        int alpha = WidgetPrefs.getInt(context, appWidgetId, "transparency", 255);
        float alphaFloat = alpha / 255.0f;

        // Получаем и применяем цвет фона
        int colorDrawable = WidgetPrefs.getInt(context, appWidgetId, "colorDrawable", R.drawable.widget_bg_black);

        // Добавляем PendingIntent для редактирования виджета
        Intent configIntent = new Intent(context, BlackDateWidgetConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_DATE_CHANGED.equals(action) ||
                Intent.ACTION_TIME_TICK.equals(action)) {

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(
                    new android.content.ComponentName(context, BlackDateWidget.class)
            );

            for (int id : ids) {
                updateDate(context, manager, id);
            }
        }
    }

}

