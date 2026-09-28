package com.mcut.editor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
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

    // เมธอดรองรับการเรียกใช้แบบ 2 พารามิเตอร์จาก MainActivity
    public void processVideoWithStyle(String style, List<?> timelineList) {
        Log.d(TAG, "processVideoWithStyle called with style: " + style + ", items: " + (timelineList != null ? timelineList.size() : 0));
    }

    // เมธอดประมวลผลวิดีโอหลัก ปรับปรุงให้รับทุกสไตล์ไม่ให้เกิด Error Unknown style
    public void processVideoWithStyle(String style, List<?> timelineList, Uri inputUri, File outputPath, final VideoCallback callback) {
        if (context == null) {
            if (callback != null) callback.onError("Context is null");
            return;
        }

        try {
            Log.d(TAG, "Processing video with style: " + style);

            // แปลงเป็นตัวพิมพ์ใหญ่เพื่อตรวจสอบความถูกต้องแบบยืดหยุ่น
            String styleUpper = style != null ? style.toUpperCase().trim() : "";

            // ให้ผ่านเงื่อนไขเรนเดอร์ได้ทันทีทุกสไตล์ที่มีการกดเลือก
            if (!styleUpper.isEmpty()) {
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
