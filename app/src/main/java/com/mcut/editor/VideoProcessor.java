package com.mcut.editor;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EditedMediaItemSequence;
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
            MediaItem mediaItem = MediaItem.fromUri(inputUri);
            EditedMediaItem editedMediaItem = new EditedMediaItem.Builder(mediaItem).build();
            
            EditedMediaItemSequence sequence = new EditedMediaItemSequence(java.util.Collections.singletonList(editedMediaItem));
            Composition composition = new Composition.Builder(sequence).build();

            transformer = new Transformer.Builder(context)
                    .setVideoMimeType(androidx.media3.common.MimeTypes.VIDEO_H264)
                    .setAudioMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC)
                    .build();

            transformer.start(composition, outputPath.getAbsolutePath());

            transformer.addListener(new Transformer.Listener() {
                @Override
                public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                    if (callback != null) {
                        callback.onSuccess(outputPath);
                    }
                }

                @Override
                public void onError(@NonNull Composition composition, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
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
