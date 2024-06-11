package com.example.serverside;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharedPreferencesUtils {
    private static final String PREFS_NAME = "ServerAppPrefs";

    /**
     * Save or update the text with the given title and date.
     *
     * @param context The context of the caller.
     * @param title The title of the text.
     * @param date The date the text was saved.
     * @param text The text to be saved or updated.
     */
    public static void saveText(Context context, String title, String date, String text) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(title + "_date", date);
        editor.putString(title + "_text", text);
        editor.apply();
    }

    /**
     * Retrieve all titles saved in SharedPreferences.
     *
     * @param context The context of the caller.
     * @return A list of all titles.
     */
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

    /**
     * Retrieve the text and date by its title.
     *
     * @param context The context of the caller.
     * @param title The title of the text.
     * @return A string array containing the date and text associated with the given title, or null if not found.
     */
    public static String[] getTextByTitle(Context context, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String date = sharedPreferences.getString(title + "_date", null);
        String text = sharedPreferences.getString(title + "_text", null);
        return new String[]{date, text};
    }

    /**
     * Remove the text and date by its title.
     *
     * @param context The context of the caller.
     * @param title The title of the text to be removed.
     */
    public static void removeTextByTitle(Context context, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(title + "_date");
        editor.remove(title + "_text");
        editor.apply();
    }
    public static void clearSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // This clears all the key-value pairs in the SharedPreferences
        editor.apply(); // Apply the changes
    }

}
