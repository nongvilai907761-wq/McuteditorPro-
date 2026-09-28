package com.mcut.editor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EditedMediaItemSequence;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;

import java.io.File;
import java.util.ArrayList;
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

    // เมธอดหลักที่รับ List ของ MediaItem จากไทม์ไลน์ (รองรับทั้งรูปภาพและวิดีโอปะปนกัน)
    public void processVideoWithStyle(String style, List<MediaItem> timelineList, Uri fallbackUri, File outputPath, final VideoCallback callback) {
        if (context == null) {
            if (callback != null) callback.onError("Context is null");
            return;
        }

        try {
            Log.d(TAG, "Processing multi-media timeline with style: " + style + ", total items: " + (timelineList != null ? timelineList.size() : 0));

            List<EditedMediaItem> editedMediaItems = new ArrayList<>();

            if (timelineList != null && !timelineList.isEmpty()) {
                // วนลูปแปลงทุก MediaItem ในไทม์ไลน์ให้เป็น EditedMediaItem
                for (MediaItem mediaItem : timelineList) {
                    EditedMediaItem editedItem = new EditedMediaItem.Builder(mediaItem).build();
                    editedMediaItems.add(editedItem);
                }
            } else if (fallbackUri != null) {
                // กรณีฉุกเฉินถ้าไทม์ไลน์ว่าง ให้ใช้ไฟล์สำรองเดี่ยวๆ
                MediaItem singleItem = MediaItem.fromUri(fallbackUri);
                editedMediaItems.add(new EditedMediaItem.Builder(singleItem).build());
            } else {
                if (callback != null) callback.onError("ไม่พบข้อมูลไฟล์มีเดียในไทม์ไลน์");
                return;
            }

            // สร้าง Sequence และ Composition (ตัดเมธอด setVideoMimeType ที่ไม่รองรับออก)
            EditedMediaItemSequence sequence = new EditedMediaItemSequence(editedMediaItems);
            Composition composition = new Composition.Builder(sequence).build();

            transformer = new Transformer.Builder(context).build();

            transformer.addListener(new Transformer.Listener() {
                @Override
                public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                    Log.d(TAG, style + " Multi-media Export Completed Successfully");
                    if (callback != null) callback.onSuccess(outputPath);
                }

                @Override
                public void onError(@NonNull Composition composition, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                    Log.e(TAG, style + " Multi-media Export Error: " + exportException.getMessage());
                    if (callback != null) callback.onError(exportException.getMessage());
                }
            });

            // เริ่มกระบวนการเรนเดอร์ Composition แบบหลายไฟล์
            transformer.start(composition, outputPath.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Exception during composition: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    public void processAndExportVideo(Uri inputUri, File outputPath, final VideoCallback callback) {
        List<MediaItem> singleList = new ArrayList<>();
        singleList.add(MediaItem.fromUri(inputUri));
        processVideoWithStyle("Default", singleList, inputUri, outputPath, callback);
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
