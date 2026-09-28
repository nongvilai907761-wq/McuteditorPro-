package com.mcut.editor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.media3.common.MimeTypes;
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

    public void processAndExportVideo(Uri inputUri, File outputPath, final VideoCallback callback) {
        if (context == null) {
            if (callback != null) callback.onError("Context is null");
            return;
        }

        try {
            androidx.media3.common.MediaItem mediaItem = androidx.media3.common.MediaItem.fromUri(inputUri);

            transformer = new Transformer.Builder(context)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .build();

            transformer.addListener(new Transformer.Listener() {
                public void onCompleted(@NonNull androidx.media3.common.MediaItem mediaItem) {
                    if (callback != null) {
                        callback.onSuccess(outputPath);
                    }
                }

                public void onError(@NonNull androidx.media3.common.MediaItem mediaItem, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
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

    // ใช้ List<?> เพื่อรองรับ MediaItem ทุกประเภทจาก MainActivity
    public void processVideoWithStyle(String style, List<?> timelineList) {
        Log.d(TAG, "processVideoWithStyle: " + style);
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
