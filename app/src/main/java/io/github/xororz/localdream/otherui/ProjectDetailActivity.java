package io.github.xororz.localdream.otherui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import io.github.xororz.localdream.MainActivity;
import io.github.xororz.localdream.R;

public class ProjectDetailActivity extends AppCompatActivity {

    private RecyclerView imagesRecycler;
    private TextView projectTitle;
    private TextView projectSubtitle;
    private TextView emptyState;
    private ProjectImageAdapter imageAdapter;
    private ProjectRepository projectRepository;
    private String projectId;
    private Project project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        projectRepository = new ProjectRepository(this);

        // Get project ID from intent
        projectId = getIntent().getStringExtra("project_id");
        if (projectId == null) {
            finish();
            return;
        }

        project = projectRepository.getProject(projectId);
        if (project == null) {
            finish();
            return;
        }

        projectTitle = findViewById(R.id.project_title);
        projectSubtitle = findViewById(R.id.project_subtitle);
        emptyState = findViewById(R.id.empty_state);
        imagesRecycler = findViewById(R.id.project_images_recycler);

        projectTitle.setText(project.getName());
        updateSubtitle();

        imagesRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new ProjectImageAdapter(uri -> {
            // When image is clicked, navigate to MainActivity to edit it
            Intent intent = new Intent(ProjectDetailActivity.this, MainActivity.class);
            intent.putExtra("image_uri", uri.toString());
            intent.putExtra("from_project", true);
            startActivity(intent);
        });
        imagesRecycler.setAdapter(imageAdapter);

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImages();
    }

    private void refreshImages() {
        project = projectRepository.getProject(projectId);
        if (project != null) {
            List<Uri> imageUris = new ArrayList<>();
            for (String uriString : project.getImageUris()) {
                imageUris.add(Uri.parse(uriString));
            }
            imageAdapter.submit(imageUris);
            updateSubtitle();

            if (imageUris.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                imagesRecycler.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                imagesRecycler.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateSubtitle() {
        int count = project.getImageCount();
        projectSubtitle.setText(count + (count == 1 ? " image" : " images"));
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_projects);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_projects) {
                startActivity(new Intent(this, ProjectsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_recent) {
                startActivity(new Intent(this, RecentEditsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_marketplace) {
                startActivity(new Intent(this, MarketplaceActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
