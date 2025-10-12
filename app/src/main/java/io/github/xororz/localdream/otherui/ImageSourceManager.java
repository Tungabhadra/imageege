package io.github.xororz.localdream.otherui;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import io.github.xororz.localdream.R;

import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles picking images from the gallery or capturing them via the camera.
 */
public class ImageSourceManager {

    public interface Listener {
        void onImagePicked(@NonNull Uri uri);

        void onImagePickError(@NonNull String message);
    }

    private final AppCompatActivity activity;
    private final Listener listener;

    private final ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<Uri> cameraLauncher;

    @Nullable
    private Uri pendingCameraUri;

    public ImageSourceManager(@NonNull AppCompatActivity activity, @NonNull Listener listener) {
        this.activity = activity;
        this.listener = listener;

        galleryLauncher = activity.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                listener.onImagePicked(uri);
            }
        });

        cameraLauncher = activity.registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (Boolean.TRUE.equals(result) && pendingCameraUri != null) {
                listener.onImagePicked(pendingCameraUri);
            } else if (pendingCameraUri != null) {
                listener.onImagePickError(activity.getString(R.string.camera_canceled_text));
            }
            pendingCameraUri = null;
        });
    }

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void selectFromGallery() {
        galleryLauncher.launch("image/*");
    }

    public void captureFromCamera() {
        pendingCameraUri = createImageUri();
        if (pendingCameraUri != null) {
            cameraLauncher.launch(pendingCameraUri);
        } else {
            listener.onImagePickError(activity.getString(R.string.camera_error_text));
        }
    }

    @Nullable
    private Uri createImageUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, buildFileName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Imagen");
            return activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File imagesDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (imagesDir == null) {
                return null;
            }
            File image;
            try {
                image = File.createTempFile(buildFileName(), ".jpg", imagesDir);
            } catch (IOException e) {
                return null;
            }
            String authority = activity.getString(R.string.file_provider_authority);
            return FileProvider.getUriForFile(activity, authority, image);
        }
    }

    private String buildFileName() {
        return "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }
}
