package io.github.xororz.localdream.otherui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import io.github.xororz.localdream.MainActivity;
import io.github.xororz.localdream.R;


import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ImageResultActivity extends AppCompatActivity {

    private ImageView resultImage;
    private Uri imageUri;
    private ProjectRepository projectRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);

        resultImage = findViewById(R.id.result_image);
        projectRepository = new ProjectRepository(this);

        // Get image URI from intent
        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            resultImage.setImageURI(imageUri);
        }

        findViewById(R.id.save_to_project_button).setOnClickListener(v -> showProjectSelectionDialog());

        findViewById(R.id.send_to_marketplace_button).setOnClickListener(v -> {
            Toast.makeText(this, "Sending to marketplace...", Toast.LENGTH_SHORT).show();
            // TODO: Implement marketplace upload
            startActivity(new Intent(this, MarketplaceActivity.class));
        });

        setupBottomNav();
    }

    private void showProjectSelectionDialog() {
        List<Project> projects = projectRepository.getProjects();
        
        if (projects.isEmpty()) {
            Toast.makeText(this, "No projects found. Create a project first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] projectNames = new String[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            projectNames[i] = projects.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Project")
                .setItems(projectNames, (dialog, which) -> {
                    Project selectedProject = projects.get(which);
                    projectRepository.addImageToProject(selectedProject.getId(), imageUri.toString());
                    Toast.makeText(this, "Image saved to " + selectedProject.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
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
