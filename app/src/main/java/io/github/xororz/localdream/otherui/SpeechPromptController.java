package io.github.xororz.localdream.otherui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Encapsulates all speech recognition wiring so the activity only has to react to callbacks.
 */
public class SpeechPromptController implements RecognitionListener {

    public interface Listener {
        void onSpeechListeningStarted();

        void onSpeechListeningFinished();

        void onSpeechError();

        void onSpeechResult(@NonNull String text);
    }

    private final Context context;
    private final Listener listener;
    @Nullable
    private SpeechRecognizer speechRecognizer;

    public SpeechPromptController(@NonNull Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onSpeechError();
            return;
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(this);
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        try {
            speechRecognizer.startListening(intent);
        } catch (ActivityNotFoundException e) {
            listener.onSpeechError();
        }
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        listener.onSpeechListeningStarted();
    }

    @Override
    public void onBeginningOfSpeech() {
        listener.onSpeechListeningStarted();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // Optional hook for visualizers.
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        listener.onSpeechListeningFinished();
    }

    @Override
    public void onError(int error) {
        listener.onSpeechError();
    }

    @Override
    public void onResults(Bundle results) {
        listener.onSpeechListeningFinished();
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            listener.onSpeechResult(matches.get(0));
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }
}
