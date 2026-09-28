package com.mcut.editor;

public class MediaItem {
    private String filePath;
    private String mediaType;

    public MediaItem() {}

    public MediaItem(String filePath, String mediaType) {
        this.filePath = filePath;
        this.mediaType = mediaType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
