package io.github.xororz.localdream.otherui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.xororz.localdream.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple adapter showing thumbnails of previously edited images.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface Listener {
        void onHistoryItemClicked(@NonNull Uri uri);
    }

    private final List<Uri> items = new ArrayList<>();
    private final Listener listener;

    public HistoryAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(List<String> data) {
        items.clear();
        for (String entry : data) {
            items.add(Uri.parse(entry));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_image, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Uri current = items.get(position);
        holder.imageView.setImageURI(current);
        holder.itemView.setOnClickListener(v -> listener.onHistoryItemClicked(current));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.history_image);
        }
    }
}
