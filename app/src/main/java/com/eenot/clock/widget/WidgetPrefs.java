package com.eenot.clock.widget;

import android.content.Context;
import android.content.SharedPreferences;

public class WidgetPrefs {
    private static final String PREF_NAME = "widget_prefs";

    public static void saveInt(Context context, int widgetId, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(key + widgetId, value).apply();
    }

    public static int getInt(Context context, int widgetId, String key, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key + widgetId, defaultValue);
    }

    public static void saveString(Context context, int widgetId, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key + widgetId, value).apply();
    }

    public static String getString(Context context, int widgetId, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key + widgetId, defaultValue);
    }
}
