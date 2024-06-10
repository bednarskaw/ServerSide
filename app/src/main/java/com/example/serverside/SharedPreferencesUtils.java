package com.example.serverside;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharedPreferencesUtils {
    private static final String PREFS_NAME = "ServerAppPrefs";

    public static void saveText(Context context, String title, String date, String text) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(title + "_date", date);
        editor.putString(title + "_text", text);
        editor.apply();
    }

    public static List<String> getAllTitles(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        List<String> titles = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_text")) {
                titles.add(key.replace("_text", ""));
            }
        }
        return titles;
    }

    public static String[] getTextByTitle(Context context, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String date = sharedPreferences.getString(title + "_date", null);
        String text = sharedPreferences.getString(title + "_text", null);
        return new String[]{date, text};
    }

    public static void removeTextByTitle(Context context, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(title + "_date");
        editor.remove(title + "_text");
        editor.apply();
    }
}
