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

    // Constructor รับ Context สำหรับระบบ Media3
    public VideoProcessor(Context context) {
        this.context = context;
    }

    // Constructor สำรอง (กรณีเรียกใช้งานแบบไม่ส่ง Context)
    public VideoProcessor() {
        this.context = null;
    }

    // --- 1. ฟังก์ชันประมวลผลวิดีโอเดิมของคุณ ---
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

            // ลบ @Override ออกชั่วคราวเพื่อป้องกัน Error เรื่องความต่างของเวอร์ชัน Media3
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

    // --- 2. ฟังก์ชันเสริมที่ MainActivity เรียกใช้งาน ---
    public void processVideoWithStyle(String style, List<MediaItem> timelineList) {
        Log.d(TAG, "processVideoWithStyle: " + style);
        // เพิ่มระบบประมวลผลสไตล์วิดีโอเพิ่มเติมตรงนี้ได้ในอนาคต
    }

    public void testFFmpegConnection() {
        Log.d(TAG, "testFFmpegConnection called");
        // ทดสอบระบบ Engine / FFmpeg
    }

    public void saveGeneratedVideo(List<MediaItem> timelineList) {
        Log.d(TAG, "saveGeneratedVideo called");
        // ระบบบันทึกวิดีโอจาก Timeline
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
