package io.github.xororz.localdream.otherui;

import android.content.Intent;
import android.os.Bundle;

import io.github.xororz.localdream.MainActivity;
import io.github.xororz.localdream.R;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecentEditsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private HistoryRepository historyRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_edits);

        historyRepository = new HistoryRepository(this);

        recyclerView = findViewById(R.id.recent_edits_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        historyAdapter = new HistoryAdapter(uri -> {
            // Navigate back to main to edit this image
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("selected_image_uri", uri.toString());
            startActivity(intent);
        });
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.submit(historyRepository.getEntries());

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_recent);
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
