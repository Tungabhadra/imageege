package io.github.xororz.localdream.otherui;

import android.content.Context;
import android.content.SharedPreferences;

import io.github.xororz.localdream.R;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks how often prompts are used and returns the most frequent ones.
 */
public class FrequentPromptRepository {

    private static final String PREFS_NAME = "frequent_prompts_prefs";
    private static final String KEY_COUNTS = "prompt_counts";
    private static final int MAX_PROMPTS = 10;

    private final SharedPreferences preferences;

    public FrequentPromptRepository(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void incrementCount(@NonNull String prompt) {
        Map<String, Integer> counts = getCounts();
        counts.put(prompt, counts.getOrDefault(prompt, 0) + 1);
        saveCounts(counts);
    }

    public String[] getTopPrompts() {
        Map<String, Integer> counts = getCounts();
        if (counts.isEmpty()) {
            return new String[0];
        }

        // Sort by count descending
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> topPrompts = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_PROMPTS, entries.size()); i++) {
            topPrompts.add(entries.get(i).getKey());
        }

        return topPrompts.toArray(new String[0]);
    }

    private Map<String, Integer> getCounts() {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, ?> all = preferences.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                counts.put(entry.getKey(), (Integer) entry.getValue());
            }
        }
        return counts;
    }

    private void saveCounts(@NonNull Map<String, Integer> counts) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            editor.putInt(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }
}
