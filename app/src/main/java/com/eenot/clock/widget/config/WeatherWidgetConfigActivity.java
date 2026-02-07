package com.eenot.clock.widget.config;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import com.eenot.clock.R;
import com.eenot.clock.widget.BlackWeatherWidget;
import com.eenot.clock.widget.BaseWidgetConfigActivity;
import com.eenot.clock.widget.WidgetPrefs;

public class WeatherWidgetConfigActivity extends BaseWidgetConfigActivity {

    private SeekBar transparencySeekBar;
    private int selectedColor = R.drawable.widget_bg_black;
    private static final int DEFAULT_TRANSPARENCY = 255;
    private static final int DEFAULT_COLOR = R.drawable.widget_bg_black;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_weather_config);

        transparencySeekBar = findViewById(R.id.seekBarTransparency);
        transparencySeekBar.setProgress(WidgetPrefs.getInt(this, widgetId, "transparency", DEFAULT_TRANSPARENCY));

        selectedColor = WidgetPrefs.getInt(this, widgetId, "colorDrawable", DEFAULT_COLOR);

        // Set up color buttons
        findViewById(R.id.colorBlack).setOnClickListener(v -> selectColor(R.drawable.widget_bg_black));
        findViewById(R.id.colorDarkGray).setOnClickListener(v -> selectColor(R.drawable.widget_bg_dark_gray));
        findViewById(R.id.colorGray).setOnClickListener(v -> selectColor(R.drawable.widget_bg_gray));
        findViewById(R.id.colorLightGray).setOnClickListener(v -> selectColor(R.drawable.widget_bg_light_gray));
        findViewById(R.id.colorRed).setOnClickListener(v -> selectColor(R.drawable.widget_bg_red));
        findViewById(R.id.colorGreen).setOnClickListener(v -> selectColor(R.drawable.widget_bg_green));
        findViewById(R.id.colorBlue).setOnClickListener(v -> selectColor(R.drawable.widget_bg_blue));
        findViewById(R.id.colorOrange).setOnClickListener(v -> selectColor(R.drawable.widget_bg_orange));

        Button resetButton = findViewById(R.id.btnReset);
        resetButton.setOnClickListener(v -> resetToDefaults());

        Button applyButton = findViewById(R.id.btnApply);
        applyButton.setOnClickListener(v -> saveAndFinish());
    }

    private void selectColor(int drawableRes) {
        selectedColor = drawableRes;
    }

    private void resetToDefaults() {
        transparencySeekBar.setProgress(DEFAULT_TRANSPARENCY);
        selectedColor = DEFAULT_COLOR;
    }

    @Override
    protected void applySettings() {
        int transparency = transparencySeekBar.getProgress();
        WidgetPrefs.saveInt(this, widgetId, "transparency", transparency);
        WidgetPrefs.saveInt(this, widgetId, "colorDrawable", selectedColor);

        // Отправляем broadcast для обновления виджета
        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.setComponent(new ComponentName(this, BlackWeatherWidget.class));
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        sendBroadcast(updateIntent);
    }
}


