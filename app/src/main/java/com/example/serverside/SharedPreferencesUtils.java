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
    public static void saveText(Context context, String title, String date, String text,  String lastEditedBy) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(title + "_date", date);
        editor.putString(title + "_text", text);
        editor.putString(title + "_last_edited_by", lastEditedBy);
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

    public static String[] getTextByTitle(Context context, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String date = sharedPreferences.getString(title + "_date", null);
        String text = sharedPreferences.getString(title + "_text", null);
        String last_edited_by = sharedPreferences.getString(title + "_last_edited_by", null);
        return new String[]{date, text, last_edited_by};
    }

    public static void removeTextByTitle(Context context, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(title + "_date");
        editor.remove(title + "_text");
        editor.remove(title + "_last_edited_by");
        editor.apply();
    }
    public static void clearSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // This clears all the key-value pairs in the SharedPreferences
        editor.apply(); // Apply the changes
    }

}
