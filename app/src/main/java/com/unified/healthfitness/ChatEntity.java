package com.unified.healthfitness;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String message;
    public boolean isUser;
    public boolean isLoading;
    public boolean isImage;
    public String imageUrl;
    public long timestamp;

    public ChatEntity(String message, boolean isUser, boolean isLoading, boolean isImage, String imageUrl, long timestamp) {
        this.message = message;
        this.isUser = isUser;
        this.isLoading = isLoading;
        this.isImage = isImage;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }
}
