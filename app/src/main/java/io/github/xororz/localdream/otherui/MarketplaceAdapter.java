package io.github.xororz.localdream.otherui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import io.github.xororz.localdream.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.MarketplaceViewHolder> {

    public interface Listener {
        void onPostClicked(@NonNull MarketplacePost post);
    }

    private final Listener listener;
    private List<MarketplacePost> posts = new ArrayList<>();

    public MarketplaceAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<MarketplacePost> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MarketplaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace_post, parent, false);
        return new MarketplaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketplaceViewHolder holder, int position) {
        MarketplacePost post = posts.get(position);
        holder.imageView.setImageURI(post.getImageUri());
        holder.promptText.setText(post.getPrompt());
        holder.authorText.setText("by " + post.getAuthor());
        holder.itemView.setOnClickListener(v -> listener.onPostClicked(post));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class MarketplaceViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView promptText;
        final TextView authorText;

        MarketplaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.marketplace_post_image);
            promptText = itemView.findViewById(R.id.marketplace_post_prompt);
            authorText = itemView.findViewById(R.id.marketplace_post_author);
        }
    }
}
