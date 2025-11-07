package com.unified.healthfitness;

public class ChatMessage {
    private final String message;
    private final boolean isUser;
    private final boolean isLoading;
    private final boolean isImage;
    private final String imageUrl;

    public ChatMessage(String message, boolean isUser) {
        this(message, isUser, false, false, null);
    }

    public ChatMessage(String message, boolean isUser, boolean isLoading) {
        this(message, isUser, isLoading, false, null);
    }

    public ChatMessage(String message, boolean isUser, boolean isLoading, boolean isImage, String imageUrl) {
        this.message = message;
        this.isUser = isUser;
        this.isLoading = isLoading;
        this.isImage = isImage;
        this.imageUrl = imageUrl;
    }

    public String getMessage() { return message; }
    public boolean isUser() { return isUser; }
    public boolean isLoading() { return isLoading; }
    public boolean isImage() { return isImage; }
    public String getImageUrl() { return imageUrl; }
}
