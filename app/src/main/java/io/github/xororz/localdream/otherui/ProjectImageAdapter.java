package io.github.xororz.localdream.otherui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import io.github.xororz.localdream.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjectImageAdapter extends RecyclerView.Adapter<ProjectImageAdapter.ViewHolder> {

    public interface Listener {
        void onImageClicked(Uri imageUri);
    }

    private List<Uri> imageUris = new ArrayList<>();
    private final Listener listener;

    public ProjectImageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Uri> newImages) {
        this.imageUris = newImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.imageView.setImageURI(imageUri);
        holder.itemView.setOnClickListener(v -> listener.onImageClicked(imageUri));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.history_image);
        }
    }
}
