package com.eenot.clock.widget;

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

import com.eenot.clock.R;
import com.eenot.clock.widget.config.WeatherWidgetConfigActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class BlackWeatherWidget extends AppWidgetProvider {

    private static final String TAG = "BlackWeatherWidget";
    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=55.7558&longitude=37.6173&current_weather=true&timezone=auto";

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    public void onUpdate(@NonNull Context context,
                         @NonNull AppWidgetManager appWidgetManager,
                         @NonNull int[] appWidgetIds) {
        startPeriodicUpdates(context, appWidgetManager, appWidgetIds);
    }

    private void startPeriodicUpdates(Context context,
                                      AppWidgetManager appWidgetManager,
                                      int[] appWidgetIds) {

        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable); // чтобы не было дублирующих обновлений
        }

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                for (int appWidgetId : appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId);
                }
                // Повтор каждые 2 минуты
                handler.postDelayed(this, 2 * 60 * 1000);
            }
        };
        handler.post(updateRunnable);
    }

    private void updateWidget(Context context,
                              AppWidgetManager appWidgetManager,
                              int appWidgetId) {

        // Получаем текущее время
        Calendar calendar = Calendar.getInstance();
        String time = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        // Подготовка PendingIntent для клика на виджет
        Intent configIntent = new Intent(context, WeatherWidgetConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Асинхронное получение температуры
        new Thread(() -> {
            String temp = fetchTemperature();
            if (temp == null) temp = "N/A"; // если ошибка сети

            String finalTemp = temp; // финальная переменная для лямбды
            handler.post(() -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.black_weather_widget);
                views.setTextViewText(R.id.widget_weather, finalTemp);
                views.setTextViewText(R.id.widget_time, time);
                views.setOnClickPendingIntent(R.id.widget_weather, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            });
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
            return null;
        }
    }

    @Override
    public void onDisabled(@NonNull Context context) {
        super.onDisabled(context);
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
            updateRunnable = null;
        }
    }
}
