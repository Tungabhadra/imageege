package io.github.xororz.localdream.otherui;

import android.net.Uri;

public class MarketplacePost {
    private final Uri imageUri;
    private final String prompt;
    private final String author;

    public MarketplacePost(Uri imageUri, String prompt, String author) {
        this.imageUri = imageUri;
        this.prompt = prompt;
        this.author = author;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getAuthor() {
        return author;
    }
}
