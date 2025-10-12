package io.github.xororz.localdream.otherui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import io.github.xororz.localdream.service.BackgroundGenerationService.*;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.github.xororz.localdream.service.BackgroundGenerationService;
import io.github.xororz.localdream.service.FlowObservationHelper;
import io.github.xororz.localdream.ui.screens.GenerationParameters;

/**
 * Placeholder for the DLC-backed image editing pipeline. Replace the TODO section with the real
 * implementation once the DLC integration is available.
 */
public class PromptImageEditor
{

    public interface Callback
    {
        void onSuccess(@NonNull Uri outputImage);

        void onError(@NonNull String message);
    }

    public void editImageWithPrompt(@NonNull Uri uri,
                                    @NonNull String prompt,
                                    @NonNull Callback callback,
                                    Context context)
    {
        String negativePrompt = "worst quality, low quality, normal quality, poorly drawn, lowres, low resolution, signature, watermarks, ugly, out of focus, error, blurry, unclear photo, bad photo, unrealistic, semi realistic, pixelated, cartoon, anime, cgi, drawing, 2d, 3d, censored, duplicate,";
        int steps = 20;               // diffusion steps
        float cfg = 7f;             // classifier-free guidance
        long seed = 123456789L;       // optional random seed
        int size = 512;               // image resolution
        float denoiseStrength = 0.6f; // for img2img or inpainting
        boolean useOpenCL = false;    // GPU acceleration flag


        File tmpFile = new File(context.getFilesDir(), "tmp.txt");

        Bitmap bitmap = getBitmapFromUri(context, uri);

        String base64String = bitmapToBase64(bitmap);

        System.out.println(base64String);



        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            fos.write(base64String.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        GenerationParameters generationParamsTmp = new GenerationParameters(
                steps = 20, cfg, 0L, prompt,
                negativePrompt, "", 512, false, denoiseStrength, null, true);

        Intent intent = new Intent(context, BackgroundGenerationService.class);

        intent.putExtra("prompt", prompt);
        intent.putExtra("negative_prompt", negativePrompt);
        intent.putExtra("steps", 28);
        intent.putExtra("cfg", cfg);
        intent.putExtra("seed", 0L);
        intent.putExtra("size", size);
        intent.putExtra("denoise_strength", 0.6f);
        intent.putExtra("use_opencl", false);
        intent.putExtra("has_image", true);

        context.startForegroundService(intent);
    }

    public String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Compress bitmap to PNG format (you can also use JPEG)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        // Convert to Base64 string
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    public Bitmap getBitmapFromUri(Context context, Uri uri) {
        if (uri == null) return null;

        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
        return bitmap;
    }
}
