package com.eenot.clock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.eenot.clock.widget.BlackDateWidget;
import com.eenot.clock.widget.BlackWeatherWidget;

public class MainActivity extends Activity {

    private WebView myWebView;
    private final String REMOTE_URL = "http://192.168.100.18:8080/ee-clock/index.html";
    private final String LOCAL_URL = "file:///android_asset/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "Загрузка интерфейса...", Toast.LENGTH_SHORT).show();

        // 1. Обновляем виджеты при запуске приложения
        updateAllWidgets();

        // 2. Инициализируем WebView
        initWebView();
    }

    private void initWebView() {
        myWebView = findViewById(R.id.myWebView); // Убедитесь, что id совпадает в XML
        WebSettings settings = myWebView.getSettings();

        // Настройки для корректной работы
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDisplayZoomControls(false);

        // Разрешаем смешанный контент (HTTP внутри HTTPS и наоборот), если нужно
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadUrl(LOCAL_URL);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl != null && failingUrl.equals(REMOTE_URL)) {
                    view.loadUrl(LOCAL_URL);
                }
            }
        });

        myWebView.loadUrl(REMOTE_URL);
    }

    private void updateAllWidgets() {
        updateWidgetProvider(WeatherClockWidget.class);
        updateWidgetProvider(BlackDateWidget.class);
        updateWidgetProvider(BlackWeatherWidget.class);
    }

    private void updateWidgetProvider(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                .getAppWidgetIds(new ComponentName(getApplicationContext(), cls));

        if (ids.length > 0) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView != null && myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}