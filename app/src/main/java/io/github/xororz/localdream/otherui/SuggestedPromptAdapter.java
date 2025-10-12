package io.github.xororz.localdream.otherui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.xororz.localdream.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Displays suggested prompts in a horizontally scrolling chip-like list.
 */
public class SuggestedPromptAdapter extends RecyclerView.Adapter<SuggestedPromptAdapter.SuggestedViewHolder> {

    public interface Listener {
        void onPromptSelected(@NonNull String prompt);
    }

    private final Listener listener;
    private String[] items = new String[0];

    public SuggestedPromptAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull String[] prompts) {
        items = prompts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_prompt, parent, false);
        return new SuggestedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedViewHolder holder, int position) {
        String prompt = items[position];
        holder.textView.setText(prompt);
        holder.itemView.setOnClickListener(v -> listener.onPromptSelected(prompt));
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    static class SuggestedViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        SuggestedViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.suggested_prompt_text);
        }
    }
}
