package com.eenot.clock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
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
    private final String LOCAL_URL = "file:///android_asset/index.html"; // http://192.168.100.18:8080/ee-clock/index.html
    private boolean isFallbackLoaded = false; // Флаг для предотвращения зацикливания

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toast.makeText(this, "Загрузка интерфейса...", Toast.LENGTH_SHORT).show();

        // 1. Обновляем виджеты при запуске приложения
        updateAllWidgets();

        // 2. Инициализируем WebView
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        myWebView = findViewById(R.id.myWebView);
        WebSettings settings = myWebView.getSettings();

        // Настройки для корректной работы
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true); // ← удалён дублирующий вызов
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // Разрешаем смешанный контент (оставлено без изменений по требованию)
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (request.isForMainFrame() && !isFallbackLoaded) {
                        isFallbackLoaded = true;
                        Toast.makeText(MainActivity.this, "Переключение на локальную версию...", Toast.LENGTH_SHORT).show();
                        view.loadUrl(LOCAL_URL);
                    } else if (isFallbackLoaded) {
                        showError("Ошибка: локальный интерфейс недоступен");
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (!isFallbackLoaded && REMOTE_URL.equals(failingUrl)) {
                    isFallbackLoaded = true;
                    Toast.makeText(MainActivity.this, "Переключение на локальную версию...", Toast.LENGTH_SHORT).show();
                    view.loadUrl(LOCAL_URL);
                } else if (isFallbackLoaded && LOCAL_URL.equals(failingUrl)) {
                    showError("Ошибка: локальный интерфейс недоступен");
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isFallbackLoaded) {
                    // Toast.makeText(MainActivity.this, "Интерфейс загружен", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myWebView.loadUrl(REMOTE_URL);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onPause() {
        super.onPause();
        if (myWebView != null) {
            myWebView.onPause();
            myWebView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myWebView != null) {
            myWebView.onResume();
            myWebView.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {
        if (myWebView != null) {
            // Останавливаем загрузку и очищаем ресурсы
            myWebView.clearHistory();
            myWebView.clearCache(true);
            myWebView.loadUrl("about:blank");
            myWebView.onPause();
            myWebView.removeAllViews();

            // Безопасное удаление из родительского контейнера
            ViewGroup parent = (ViewGroup) myWebView.getParent();
            if (parent != null) {
                parent.removeView(myWebView);
            }

            myWebView.destroy();
            myWebView = null;
        }
        super.onDestroy();
    }
}