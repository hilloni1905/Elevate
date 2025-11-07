package com.unified.healthfitness;

public class PexelsResponse {
    public PexelsVideo[] videos;

    public static class PexelsVideo {
        public int id;
        public VideoFile[] video_files;
    }

    public static class VideoFile {
        public String link;
        public String quality;
        public int width;
        public int height;
    }
}