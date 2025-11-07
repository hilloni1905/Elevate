package com.unified.healthfitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_LOADING = 3;
    private static final int VIEW_TYPE_IMAGE = 4;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage m = messages.get(position);
        if (m.isLoading()) return VIEW_TYPE_LOADING;
        if (m.isImage()) return VIEW_TYPE_IMAGE;
        return m.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View v = inflater.inflate(R.layout.activity_item_user_message, parent, false);
            return new UserViewHolder(v);
        } else if (viewType == VIEW_TYPE_BOT) {
            View v = inflater.inflate(R.layout.activity_item_bot_message, parent, false);
            return new BotViewHolder(v);
        } else if (viewType == VIEW_TYPE_IMAGE) {
            View v = inflater.inflate(R.layout.activity_item_image_message, parent, false);
            return new ImageViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.activity_item_loading, parent, false);
            return new LoadingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).messageText.setText(m.getMessage());
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).messageText.setText(m.getMessage());
        } else if (holder instanceof ImageViewHolder) {
            ImageView iv = ((ImageViewHolder) holder).imageView;
            String url = m.getImageUrl();
            // load with Glide
            Glide.with(iv.getContext()).load(url).into(iv);
        } // loading needs no binding
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        UserViewHolder(View v) {
            super(v);
            messageText = v.findViewById(R.id.userMessage);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        BotViewHolder(View v) {
            super(v);
            messageText = v.findViewById(R.id.botMessage);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.generatedImage);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        LoadingViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.loadingProgress);
        }
    }
}
