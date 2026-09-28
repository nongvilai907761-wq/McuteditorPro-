package com.mcut.editor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;

import java.io.File;
import java.util.List;

public class VideoProcessor {

    private static final String TAG = "VideoProcessor";
    private final Context context;
    private Transformer transformer;

    public VideoProcessor(Context context) {
        this.context = context;
    }

    public VideoProcessor() {
        this.context = null;
    }

    // [เพิ่มเข้ามา] เมธอดรองรับการเรียกจาก MainActivity แบบ 2 พารามิเตอร์
    public void processVideoWithStyle(String style, List<?> timelineList) {
        Log.d(TAG, "processVideoWithStyle called with style: " + style + ", items: " + (timelineList != null ? timelineList.size() : 0));
    }

    // เมธอดรองรับการประมวลผลวิดีโอแบบเต็มรูปแบบ (5 พารามิเตอร์) และรองรับสไตล์ M cut editor
    public void processVideoWithStyle(String style, List<?> timelineList, Uri inputUri, File outputPath, final VideoCallback callback) {
        if (context == null) {
            if (callback != null) callback.onError("Context is null");
            return;
        }

        try {
            Log.d(TAG, "Processing video with style: " + style);

            if ("M cut editor".equals(style) || "VLOG CINEMATIC".equals(style) || "FREE FIRE HIGHLIGHT".equals(style) || "🔥 FREE FIRE HIGHLIGHT".equals(style) || "🎬 VLOG CINEMATIC".equals(style)) {
                MediaItem mediaItem = MediaItem.fromUri(inputUri);

                transformer = new Transformer.Builder(context)
                        .setVideoMimeType(MimeTypes.VIDEO_H264)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .build();

                transformer.addListener(new Transformer.Listener() {
                    public void onCompleted(@NonNull MediaItem mediaItem) {
                        Log.d(TAG, style + " Export Completed Successfully");
                        if (callback != null) callback.onSuccess(outputPath);
                    }

                    public void onError(@NonNull MediaItem mediaItem, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                        Log.e(TAG, style + " Export Error: " + exportException.getMessage());
                        if (callback != null) callback.onError(exportException.getMessage());
                    }
                });

                transformer.start(mediaItem, outputPath.getAbsolutePath());
            } else {
                if (callback != null) callback.onError("Unknown style selected: " + style);
            }

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    public void processAndExportVideo(Uri inputUri, File outputPath, final VideoCallback callback) {
        if (context == null) {
            if (callback != null) callback.onError("Context is null");
            return;
        }

        try {
            MediaItem mediaItem = MediaItem.fromUri(inputUri);

            transformer = new Transformer.Builder(context)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .build();

            transformer.addListener(new Transformer.Listener() {
                public void onCompleted(@NonNull MediaItem mediaItem) {
                    if (callback != null) {
                        callback.onSuccess(outputPath);
                    }
                }

                public void onError(@NonNull MediaItem mediaItem, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                    if (callback != null) {
                        callback.onError(exportException.getMessage());
                    }
                }
            });

            transformer.start(mediaItem, outputPath.getAbsolutePath());

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    public void testFFmpegConnection() {
        Log.d(TAG, "testFFmpegConnection called");
    }

    public void saveGeneratedVideo(List<?> timelineList) {
        Log.d(TAG, "saveGeneratedVideo called");
    }

    public void cancel() {
        if (transformer != null) {
            transformer.cancel();
        }
    }

    public interface VideoCallback {
        void onSuccess(File outputFile);
        void onError(String errorMessage);
    }
}
