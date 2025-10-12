package io.github.xororz.localdream.otherui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import io.github.xororz.localdream.MainActivity;
import io.github.xororz.localdream.R;

public class MarketplaceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MarketplaceAdapter marketplaceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        recyclerView = findViewById(R.id.marketplace_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        marketplaceAdapter = new MarketplaceAdapter(post -> {
            Toast.makeText(this, "Prompt: " + post.getPrompt(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(marketplaceAdapter);

        // Load sample posts (in real app, fetch from server)
        loadSamplePosts();

        findViewById(R.id.marketplace_upload_button).setOnClickListener(v -> {
            Toast.makeText(this, "Upload feature coming soon", Toast.LENGTH_SHORT).show();
        });

        setupBottomNav();
    }

    private void loadSamplePosts() {
        List<MarketplacePost> samplePosts = new ArrayList<>();
        // In a real app, fetch from server/database
        // For now, just show empty list
        marketplaceAdapter.submit(samplePosts);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_marketplace);
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
                return true;
            }
            return false;
        });
    }
}
