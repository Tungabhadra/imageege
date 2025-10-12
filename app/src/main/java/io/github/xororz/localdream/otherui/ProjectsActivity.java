package io.github.xororz.localdream.otherui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import io.github.xororz.localdream.MainActivity;
import io.github.xororz.localdream.R;


import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProjectsActivity extends AppCompatActivity {

    private RecyclerView projectsRecycler;
    private ProjectAdapter projectAdapter;
    private ProjectRepository projectRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        projectRepository = new ProjectRepository(this);

        projectsRecycler = findViewById(R.id.projects_recycler);
        projectsRecycler.setLayoutManager(new LinearLayoutManager(this));

        projectAdapter = new ProjectAdapter(new ProjectAdapter.Listener() {
            @Override
            public void onProjectClicked(Project project) {
                // Navigate to project detail view to see all images
                Intent intent = new Intent(ProjectsActivity.this, ProjectDetailActivity.class);
                intent.putExtra("project_id", project.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Project project) {
                new MaterialAlertDialogBuilder(ProjectsActivity.this)
                        .setTitle("Delete Project")
                        .setMessage("Are you sure you want to delete \"" + project.getName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            projectRepository.deleteProject(project.getId());
                            refreshProjects();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        projectsRecycler.setAdapter(projectAdapter);

        findViewById(R.id.projects_create_button).setOnClickListener(v -> showCreateProjectDialog());

        refreshProjects();
        setupBottomNav();
    }

    private void showCreateProjectDialog() {
        EditText input = new EditText(this);
        input.setHint("Project name");
        input.setPadding(48, 32, 48, 32);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create New Project")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        projectRepository.addProject(name);
                        refreshProjects();
                        Toast.makeText(this, "Project created", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshProjects() {
        projectAdapter.submit(projectRepository.getProjects());
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
