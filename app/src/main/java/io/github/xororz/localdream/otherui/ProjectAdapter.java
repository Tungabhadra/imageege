package io.github.xororz.localdream.otherui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.xororz.localdream.R;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface Listener {
        void onProjectClicked(@NonNull Project project);
        void onDeleteClicked(@NonNull Project project);
    }

    private final Listener listener;
    private List<Project> projects = new ArrayList<>();

    public ProjectAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.nameText.setText(project.getName());
        holder.imageCountText.setText(project.getImageCount() + " images");
        holder.itemView.setOnClickListener(v -> listener.onProjectClicked(project));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(project));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView imageCountText;
        final ImageView deleteButton;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.project_name);
            imageCountText = itemView.findViewById(R.id.project_image_count);
            deleteButton = itemView.findViewById(R.id.project_delete_button);
        }
    }
}
