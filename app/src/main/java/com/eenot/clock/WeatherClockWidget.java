package com.eenot.clock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.eenot.clock.widget.WidgetPrefs;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class WeatherClockWidget extends AppWidgetProvider {

    private static final String TAG = "WeatherClockWidget";
    private static final String ACTION_UPDATE_TIME = "com.eenot.clock.UPDATE_TIME";
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast?latitude=55.7558&longitude=37.6173&current_weather=true&timezone=auto";

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        // Запускаем периодическое обновление времени и погоды
        startPeriodicUpdates(context, appWidgetManager, appWidgetIds);
    }

    private void startPeriodicUpdates(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                for (int appWidgetId : appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId);
                }
                // Повтор каждые 30 секунд для теста (погода + время)
                handler.postDelayed(this, 120 * 1000);
            }
        };
        handler.post(updateRunnable);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_clock_widget);

        // Обновляем время сразу
        Calendar calendar = Calendar.getInstance();
        String time = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        views.setTextViewText(R.id.widget_time, time);

        // Показываем индикатор загрузки температуры
        views.setTextViewText(R.id.widget_temperature, "...");

        // Получаем и применяем прозрачность к фону (ImageView)
        int alpha = WidgetPrefs.getInt(context, appWidgetId, "transparency", 255);
        float alphaFloat = alpha / 255.0f;

        // Получаем и применяем цвет фона
        int colorDrawable = WidgetPrefs.getInt(context, appWidgetId, "colorDrawable", R.drawable.widget_bg_black);

        // Добавляем PendingIntent для редактирования виджета
        Intent configIntent = new Intent(context, com.eenot.clock.widget.config.WeatherClockWidgetConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.weather_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Загружаем температуру в отдельном потоке
        new Thread(() -> {
            String temp = fetchTemperature();
            if (temp != null) {
                RemoteViews updatedViews = new RemoteViews(context.getPackageName(), R.layout.weather_clock_widget);
                updatedViews.setTextViewText(R.id.widget_temperature, temp);
                updatedViews.setTextViewText(R.id.widget_time, time); // время тоже обновляем
                
                // Добавляем PendingIntent и в обновленные views
                updatedViews.setOnClickPendingIntent(R.id.weather_container, pendingIntent);
                
                appWidgetManager.updateAppWidget(appWidgetId, updatedViews);
            }
        }).start();
    }

    private String fetchTemperature() {
        try {
            URL url = new URL(WEATHER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            connection.disconnect();

            JSONObject data = new JSONObject(json.toString());
            JSONObject currentWeather = data.getJSONObject("current_weather");
            double temperature = currentWeather.getDouble("temperature");
            return String.format("%.0f°", temperature);

        } catch (Exception e) {
            Log.e(TAG, "Error fetching weather", e);
            return "N/A";
        }
    }

    @Override
    public void onDisabled(@NonNull Context context) {
        super.onDisabled(context);
        // Отменяем Handler при удалении всех виджетов
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
}

