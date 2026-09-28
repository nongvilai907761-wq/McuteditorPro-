package com.mcut.editor;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;
import java.io.File;

public class VideoProcessor {

    private final Context context;
    private Transformer transformer;

    public VideoProcessor(Context context) {
        this.context = context;
    }

    public void processAndExportVideo(Uri inputUri, File outputPath, final VideoCallback callback) {
        try {
            // สร้าง MediaItem จาก Uri ที่ผู้ใช้เลือก
            MediaItem mediaItem = MediaItem.fromUri(inputUri);

            // ตั้งค่า Transformer สำหรับแปลงและตัดต่อวิดีโอ (รองรับ Media3 เวอร์ชัน 1.2.0)
            transformer = new Transformer.Builder(context)
                    .setVideoMimeType(androidx.media3.common.MimeTypes.VIDEO_H264)
                    .setAudioMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC)
                    .build();

            // เริ่มกระบวนการแปลงไฟล์วิดีโอไปยังตำแหน่งปลายทาง
            transformer.start(mediaItem, outputPath.getAbsolutePath());

            transformer.addListener(new Transformer.Listener() {
                @Override
                public void onCompleted(@NonNull MediaItem mediaItem, @NonNull ExportResult exportResult) {
                    if (callback != null) {
                        callback.onSuccess(outputPath);
                    }
                }

                @Override
                public void onError(@NonNull MediaItem mediaItem, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                    if (callback != null) {
                        callback.onError(exportException.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
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
