package io.github.xororz.localdream.otherui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ProjectRepository {

    private static final String PREFS_NAME = "projects_prefs";
    private static final String KEY_PROJECTS = "projects_json";

    private final SharedPreferences preferences;
    private final Gson gson;

    public ProjectRepository(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Project> getProjects() {
        String json = preferences.getString(KEY_PROJECTS, "[]");
        Type type = new TypeToken<List<Project>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addProject(@NonNull String name) {
        List<Project> projects = getProjects();
        String id = UUID.randomUUID().toString();
        projects.add(new Project(id, name));
        saveProjects(projects);
    }

    public void deleteProject(@NonNull String projectId) {
        List<Project> projects = getProjects();
        projects.removeIf(p -> p.getId().equals(projectId));
        saveProjects(projects);
    }

    public void addImageToProject(@NonNull String projectId, @NonNull String imageUri) {
        List<Project> projects = getProjects();
        for (Project project : projects) {
            if (project.getId().equals(projectId)) {
                project.addImage(imageUri);
                break;
            }
        }
        saveProjects(projects);
    }

    public Project getProject(@NonNull String projectId) {
        List<Project> projects = getProjects();
        for (Project project : projects) {
            if (project.getId().equals(projectId)) {
                return project;
            }
        }
        return null;
    }

    private void saveProjects(@NonNull List<Project> projects) {
        String json = gson.toJson(projects);
        preferences.edit().putString(KEY_PROJECTS, json).apply();
    }
}
