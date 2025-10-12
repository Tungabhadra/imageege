package io.github.xororz.localdream.otherui;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private final String id;
    private final String name;
    private final List<String> imageUris;

    public Project(String id, String name) {
        this.id = id;
        this.name = name;
        this.imageUris = new ArrayList<>();
    }

    public Project(String id, String name, List<String> imageUris) {
        this.id = id;
        this.name = name;
        this.imageUris = imageUris;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public void addImage(String uri) {
        imageUris.add(uri);
    }

    public int getImageCount() {
        return imageUris.size();
    }
}
