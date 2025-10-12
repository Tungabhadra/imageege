package io.github.xororz.localdream;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;

import io.github.xororz.localdream.otherui.FrequentPromptRepository;
import io.github.xororz.localdream.otherui.HistoryRepository;
import io.github.xororz.localdream.otherui.ImageResultActivity;
import io.github.xororz.localdream.otherui.ImageSourceManager;
import io.github.xororz.localdream.otherui.MarketplaceActivity;
import io.github.xororz.localdream.otherui.ProjectsActivity;
import io.github.xororz.localdream.otherui.PromptImageEditor;
import io.github.xororz.localdream.otherui.RecentEditsActivity;
import io.github.xororz.localdream.otherui.SpeechPromptController;
import io.github.xororz.localdream.otherui.SuggestedPromptAdapter;
import io.github.xororz.localdream.service.BackendService;
import io.github.xororz.localdream.service.BackgroundGenerationService;
import io.github.xororz.localdream.service.FlowObservationHelper;

/**
 * Main entry point that wires up image selection, prompt handling, and the speech recognizer UI.
 * The actual DLC-backed image editing pipeline is left as TODOs inside {@link PromptImageEditor}.
 */
public class MainActivity extends AppCompatActivity implements
        SpeechPromptController.Listener
{

    private static final int REQUEST_CODE_RECORD_AUDIO = 2001;

    private ImageView imagePreview;
    private View imagePreviewContainer;
    private View uploadButtonsContainer;
    private EditText promptInput;
    private ProgressBar progressBar;
    private RecyclerView suggestionsRecycler;
    private ImageView micButton;
    private View generateButton;

    private Uri selectedImageUri;
    private SpeechPromptController speechPromptController;
    private ImageSourceManager imageSourceManager;
    private HistoryRepository historyRepository;
    private SuggestedPromptAdapter suggestedPromptAdapter;
    private FrequentPromptRepository frequentPromptRepository;
    private String[] suggestedPrompts = new String[0];
    private final Handler handler = new Handler();

    private final PromptImageEditor promptImageEditor = new PromptImageEditor();
    private ActivityResultLauncher<String[]> mediaPermissionLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private boolean pendingGalleryRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermissionLaunchers();
        bindViews();
        setupControllers();
        setupInteractions();

        Intent intent = new Intent(this, BackendService.class);

        intent.putExtra("modelId", "customsd");
        intent.putExtra("resolution", 512);
        intent.putExtra("use_opencl", false);
        startForegroundService(intent);

        FlowObservationHelper.observeGenerationState(
                this, // LifecycleOwner
                state ->
                {
                    if (state instanceof BackgroundGenerationService.GenerationState.Idle)
                    {
                        runOnUiThread(() ->
                        {
                            progressBar.setVisibility(View.GONE);
                            generateButton.setEnabled(true);
                        });

                    }
                    else if (state instanceof BackgroundGenerationService.GenerationState.Progress)
                    {
                        float progress = ((BackgroundGenerationService.GenerationState.Progress) state).getProgress();
                        runOnUiThread(() ->
                        {
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress((int) (progress * 100));
                            generateButton.setEnabled(false);
                        });

                    }
                    else if (state instanceof BackgroundGenerationService.GenerationState.Complete)
                    {
                        Bitmap bitmap = ((BackgroundGenerationService.GenerationState.Complete) state).getBitmap();
                        runOnUiThread(() ->
                        {
                            progressBar.setVisibility(View.GONE);
                            generateButton.setEnabled(true);
                            imagePreview.setImageBitmap(bitmap);
                            Toast.makeText(MainActivity.this, "Generation Complete!", Toast.LENGTH_SHORT).show();

                            // Save to history and track prompt usage

                            Uri uri = saveBitmapToFile(bitmap);

                            historyRepository.addEntry(uri);
                            frequentPromptRepository.incrementCount(promptInput.getText().toString());

                            // Navigate to result page
                            Intent result_intent = new Intent(MainActivity.this, ImageResultActivity.class);
                            result_intent.putExtra("image_uri", uri.toString());
                            startActivity(result_intent);
                        });



                        BackgroundGenerationService.Companion.clearCompleteState();

                    }
                    else if (state instanceof BackgroundGenerationService.GenerationState.Error)
                    {
                        String msg = ((BackgroundGenerationService.GenerationState.Error) state).getMessage();
                        runOnUiThread(() ->
                        {
                            progressBar.setVisibility(View.GONE);
                            generateButton.setEnabled(true);
                            Toast.makeText(MainActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        });
                    }

                    return null; // for Kotlin Unit
                }
        );


        // Check if we're coming from a project with an image to edit
        handleIncomingIntent();
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        Uri uri = null;
        try {
            // Use app-specific cache directory
            File cachePath = new File(getCacheDir(), "images");
            if (!cachePath.exists()) {
                cachePath.mkdirs();
            }

            // Create a file with unique name
            File file = new File(cachePath, "generated_" + System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            // Convert file path to Uri
            uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider", // must match authority in Manifest
                    file
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }


    private void handleIncomingIntent()
    {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("image_uri"))
        {
            String uriString = intent.getStringExtra("image_uri");
            if (uriString != null)
            {
                selectedImageUri = Uri.parse(uriString);
                showImagePreview(selectedImageUri);
            }
        }
    }


    private void bindViews()
    {
        imagePreview = findViewById(R.id.image_preview);
        imagePreviewContainer = findViewById(R.id.image_preview_container);
        uploadButtonsContainer = findViewById(R.id.upload_buttons_container);
        promptInput = findViewById(R.id.prompt_input);
        progressBar = findViewById(R.id.progress_bar);
        View galleryButton = findViewById(R.id.pick_image_button);
        View cameraButton = findViewById(R.id.capture_image_button);
        View removeImageButton = findViewById(R.id.remove_image_button);
        suggestionsRecycler = findViewById(R.id.suggested_recycler);
        micButton = findViewById(R.id.mic_button);
        generateButton = findViewById(R.id.generate_button);

        galleryButton.setOnClickListener(v -> handleGalleryClick());
        cameraButton.setOnClickListener(v -> handleCameraClick());
        removeImageButton.setOnClickListener(v -> handleRemoveImage());
        generateButton.setOnClickListener(v -> handleApplyPrompt());
    }

    private void setupControllers()
    {
        historyRepository = new HistoryRepository(this);
        imageSourceManager = new ImageSourceManager(this, new ImageSourceManager.Listener()
        {
            @Override
            public void onImagePicked(@NonNull Uri uri)
            {
                selectedImageUri = uri;
                grantPersistableAccess(uri);
                showImagePreview(uri);
            }

            @Override
            public void onImagePickError(@NonNull String message)
            {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        speechPromptController = new SpeechPromptController(this, this);

        suggestedPrompts = getResources().getStringArray(R.array.suggested_prompts);
        suggestedPromptAdapter = new SuggestedPromptAdapter(prompt ->
        {
            promptInput.setText(prompt);
            promptInput.setSelection(prompt.length());
        });
        suggestionsRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        suggestionsRecycler.setAdapter(suggestedPromptAdapter);
        suggestedPromptAdapter.submit(suggestedPrompts);

        frequentPromptRepository = new FrequentPromptRepository(this);

        // Mic button - hold to speak with scale animation
        micButton.setOnTouchListener((v, event) ->
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    // Animate button getting bigger
                    v.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(200)
                            .start();

                    // User pressed down - start listening
                    if (ensureAudioPermission())
                    {
                        speechPromptController.startListening();
                        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Animate button back to normal size
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start();

                    // User released - stop listening
                    speechPromptController.stopListening();
                    return true;
            }
            return false;
        });

        setupBottomNav();
    }

    private void setupBottomNav()
    {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item ->
        {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home)
            {
                return true;
            }
            else if (itemId == R.id.nav_projects)
            {
                startActivity(new Intent(this, ProjectsActivity.class));
                finish();
                return true;
            }
            else if (itemId == R.id.nav_recent)
            {
                startActivity(new Intent(this, RecentEditsActivity.class));
                finish();
                return true;
            }
            else if (itemId == R.id.nav_marketplace)
            {
                startActivity(new Intent(this, MarketplaceActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupInteractions()
    {
        // Mic button is now handled via promptInputLayout.setEndIconOnClickListener
    }

    private void showImagePreview(@NonNull Uri uri)
    {
        imagePreview.setImageURI(uri);
        imagePreviewContainer.setVisibility(View.VISIBLE);
        uploadButtonsContainer.setVisibility(View.GONE);
    }

    private void handleRemoveImage()
    {
        selectedImageUri = null;
        imagePreview.setImageURI(null);
        imagePreviewContainer.setVisibility(View.GONE);
        uploadButtonsContainer.setVisibility(View.VISIBLE);
        promptInput.setText("");
    }

    private void handleApplyPrompt()
    {
        if (selectedImageUri == null)
        {
            Toast.makeText(this, R.string.select_image_first_text, Toast.LENGTH_SHORT).show();
            return;
        }

        final String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty())
        {
            Toast.makeText(this, R.string.enter_prompt_text, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Simulate the async DLC call so UI flow is testable.
        handler.postDelayed(() ->
        {
            progressBar.setVisibility(View.GONE);

            // TODO: Replace this block with the real call to promptImageEditor once DLC binding exists.
            promptImageEditor.editImageWithPrompt(selectedImageUri, prompt, new PromptImageEditor.Callback()
            {
                @Override
                public void onSuccess(Uri outputImage)
                {
                    // Save to history and track prompt usage
                    historyRepository.addEntry(outputImage);
                    frequentPromptRepository.incrementCount(prompt);

                    // Navigate to result page
                    Intent intent = new Intent(MainActivity.this, ImageResultActivity.class);
                    intent.putExtra("image_uri", outputImage.toString());
                    startActivity(intent);
                }

                @Override
                public void onError(String message)
                {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }, this);
        }, 600);
    }

    private boolean ensureAudioPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE_RECORD_AUDIO);
        return false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (speechPromptController != null)
        {
            speechPromptController.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_RECORD_AUDIO)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                speechPromptController.startListening();
            }
            else
            {
                Toast.makeText(this, R.string.audio_permission_denied_text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initPermissionLaunchers()
    {
        mediaPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result ->
                {
                    boolean granted = false;
                    for (Boolean value : result.values())
                    {
                        if (Boolean.TRUE.equals(value))
                        {
                            granted = true;
                            break;
                        }
                    }
                    if (granted && pendingGalleryRequest)
                    {
                        pendingGalleryRequest = false;
                        imageSourceManager.selectFromGallery();
                    }
                    else if (pendingGalleryRequest)
                    {
                        pendingGalleryRequest = false;
                        Toast.makeText(this, R.string.media_permission_denied_text, Toast.LENGTH_SHORT).show();
                    }
                });

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result ->
                {
                    Boolean granted = result.get(Manifest.permission.CAMERA);
                    if (Boolean.TRUE.equals(granted))
                    {
                        imageSourceManager.captureFromCamera();
                    }
                    else
                    {
                        Toast.makeText(this, R.string.camera_permission_denied_text, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleGalleryClick()
    {
        if (ensureImagePermission())
        {
            imageSourceManager.selectFromGallery();
            pendingGalleryRequest = false;
        }
        else
        {
            pendingGalleryRequest = true;
        }
    }

    private void handleCameraClick()
    {
        if (imageSourceManager.hasCameraPermission())
        {
            imageSourceManager.captureFromCamera();
        }
        else
        {
            cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }

    private boolean ensureImagePermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            mediaPermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            return false;
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            mediaPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            return false;
        }
    }

    private void grantPersistableAccess(@NonNull Uri uri)
    {
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try
        {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
        catch (SecurityException ignored)
        {
            // Provider might not support persistable permissions.
        }
    }

    @Override
    public void onSpeechListeningStarted()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSpeechListeningFinished()
    {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSpeechError()
    {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, R.string.speech_not_supported, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSpeechResult(@NonNull String text)
    {
        promptInput.setText(text);
    }
}
