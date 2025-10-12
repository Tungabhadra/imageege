package io.github.xororz.localdream.otherui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Very lightweight persistence layer using SharedPreferences to keep a history of edited images.
 * Replace with Room or another store if you need richer metadata.
 */
public class HistoryRepository {

    private static final String PREFS_NAME = "image_history_prefs";
    private static final String KEY_HISTORY = "history_uris";
    private static final int MAX_ENTRIES = 12;

    private final SharedPreferences preferences;

    public HistoryRepository(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addEntry(@NonNull Uri uri) {
        List<String> current = new ArrayList<>(getEntries());
        current.remove(uri.toString());
        current.add(0, uri.toString());
        if (current.size() > MAX_ENTRIES) {
            current = current.subList(0, MAX_ENTRIES);
        }
        saveEntries(current);
    }

    public List<String> getEntries() {
        String stored = preferences.getString(KEY_HISTORY, "");
        if (stored == null || stored.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(stored.split("\n")));
    }

    @Nullable
    public Uri getFirst() {
        List<String> entries = getEntries();
        if (entries.isEmpty()) {
            return null;
        }
        return Uri.parse(entries.get(0));
    }

    private void saveEntries(@NonNull List<String> entries) {
        String joined = TextUtils.join("\n", entries);
        preferences.edit().putString(KEY_HISTORY, joined).apply();
    }
}
