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

    // เมธอดหลักรองรับทั้งรูปภาพและวิดีโอสลับกันในไทม์ไลน์
    public void processVideoWithStyle(String style, List<MediaItem> timelineList, Uri fallbackUri, File outputPath, final VideoCallback callback) {
        if (context == null) {
            if (callback != null) callback.onError("Context is null");
            return;
        }

        try {
            Log.d(TAG, "Processing mixed timeline with style: " + style + ", total items: " + (timelineList != null ? timelineList.size() : 0));

            List<EditedMediaItem> editedMediaItems = new ArrayList<>();

            if (timelineList != null && !timelineList.isEmpty()) {
                for (MediaItem mediaItem : timelineList) {
                    // ตรวจสอบประเภทไฟล์ว่าเป็นรูปภาพหรือไม่จาก URI หรือประเภทที่มีการตั้งค่า
                    boolean isImage = isImageItem(mediaItem);

                    MediaItem.Builder mediaItemBuilder = mediaItem.buildUpon();
                    EditedMediaItem.Builder editedItemBuilder;

                    if (isImage) {
                        // กำหนดให้รูปภาพแปลงเป็นวิดีโอสั้น แสดงผลรูปละ 3 วินาที (3000 มิลลิวินาที)
                        mediaItemBuilder.setImageDurationMs(3000);
                        editedItemBuilder = new EditedMediaItem.Builder(mediaItemBuilder.build());
                    } else {
                        // กรณีเป็นวิดีโอใช้งานปกติ
                        editedItemBuilder = new EditedMediaItem.Builder(mediaItem);
                    }

                    editedMediaItems.add(editedItemBuilder.build());
                }
            } else if (fallbackUri != null) {
                MediaItem singleItem = MediaItem.fromUri(fallbackUri);
                editedMediaItems.add(new EditedMediaItem.Builder(singleItem).build());
            } else {
                if (callback != null) callback.onError("ไม่พบข้อมูลไฟล์มีเดียในไทม์ไลน์");
                return;
            }

            EditedMediaItemSequence sequence = new EditedMediaItemSequence(editedMediaItems);
            Composition composition = new Composition.Builder(sequence).build();

            transformer = new Transformer.Builder(context).build();

            transformer.addListener(new Transformer.Listener() {
                @Override
                public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                    Log.d(TAG, style + " Mixed Media Export Completed Successfully");
                    if (callback != null) callback.onSuccess(outputPath);
                }

                @Override
                public void onError(@NonNull Composition composition, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                    Log.e(TAG, style + " Mixed Media Export Error: " + exportException.getMessage());
                    if (callback != null) callback.onError(exportException.getMessage());
                }
            });

            transformer.start(composition, outputPath.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Exception during mixed composition: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    // ฟังก์ชันช่วยเช็คว่าเป็นไฟล์รูปภาพหรือไม่
    private boolean isImageItem(MediaItem mediaItem) {
        if (mediaItem.localConfiguration != null && mediaItem.localConfiguration.uri != null) {
            String uriString = mediaItem.localConfiguration.uri.toString().toLowerCase();
            return uriString.endsWith(".jpg") || uriString.endsWith(".jpeg") || 
                   uriString.endsWith(".png") || uriString.endsWith(".webp") || 
                   uriString.contains("image");
        }
        return false;
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
