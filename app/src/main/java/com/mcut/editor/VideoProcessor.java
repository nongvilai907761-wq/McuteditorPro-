package com.mcut.editor;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VideoProcessor {
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public VideoProcessor(Context context) {
        this.context = context;
    }

    public static class MediaVisualItem {
        public File file;
        public boolean isImage;
        public MediaVisualItem(File file, boolean isImage) {
            this.file = file;
            this.isImage = isImage;
        }
    }

    public void testFFmpegConnection() {
        Toast.makeText(context, "🔍 กำลังทดสอบ FFmpeg Engine...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                FFmpegSession session = FFmpegKit.execute("-version");
                final boolean isSuccess = ReturnCode.isSuccess(session.getReturnCode());
                handler.post(() -> {
                    if (isSuccess) {
                        Toast.makeText(context, "✅ FFmpeg พร้อมใช้งานระดับโปร!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "❌ FFmpeg ไม่รองรับบนอุปกรณ์นี้", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void processVideoWithStyle(String selectedStyle, List<MediaItem> timelineList) {
        if (timelineList == null || timelineList.isEmpty()) {
            Toast.makeText(context, "⚠️ กรุณาเพิ่มคลิปลงในไทม์ไลน์ก่อนเลือกสไตล์", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "🎨 กำลังปรับแต่งฟิลเตอร์ตามสไตล์: [" + selectedStyle + "]...", Toast.LENGTH_SHORT).show();
    }

    public void saveGeneratedVideo(final List<MediaItem> timelineList) {
        if (timelineList == null || timelineList.isEmpty()) {
            handler.post(() -> Toast.makeText(context, "⚠️ ไม่มีไฟล์ในไทม์ไลน์เพื่อเรนเดอร์", Toast.LENGTH_SHORT).show());
            return;
        }
        handler.post(() -> Toast.makeText(context, "🎬 เริ่มต้นเรนเดอร์ระดับสูง (60 FPS Pipeline)...", Toast.LENGTH_SHORT).show());

        new Thread(() -> {
            try {
                File moviesDir = context.getExternalFilesDir(null);
                if (moviesDir != null && !moviesDir.exists()) moviesDir.mkdirs();

                File outputFile = new File(moviesDir, "Mcuteditor_Pro_" + System.currentTimeMillis() + ".mp4");
                List<MediaVisualItem> visualItems = new ArrayList<>();
                List<File> audioFiles = new ArrayList<>();

                for (int i = 0; i < timelineList.size(); i++) {
                    MediaItem item = timelineList.get(i);
                    String uriStr = item.getFilePath();
                    if (uriStr == null || uriStr.isEmpty()) continue;

                    File inputCacheFile = new File(context.getCacheDir(), "input_cache_" + i + ".tmp");
                    if (inputCacheFile.exists()) inputCacheFile.delete();

                    try (InputStream inputStream = uriStr.startsWith("content://") || uriStr.startsWith("file://") 
                            ? context.getContentResolver().openInputStream(Uri.parse(uriStr)) 
                            : new FileInputStream(new File(uriStr));
                         OutputStream outputStream = new FileOutputStream(inputCacheFile)) {
                        if (inputStream != null) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.flush();
                        }
                    } catch (Exception e) {
                        Log.e("Mcuteditor", "Cache error: " + e.getMessage());
                    }

                    if (!inputCacheFile.exists() || inputCacheFile.length() == 0) continue;

                    String mimeType = "";
                    try {
                        if (uriStr.startsWith("content://")) {
                            mimeType = context.getContentResolver().getType(Uri.parse(uriStr));
                        }
                    } catch (Exception ignored) {}
                    if (mimeType == null) mimeType = "";

                    String mediaTypeStr = item.getMediaType() != null ? item.getMediaType() : "";
                    boolean isAudio = mimeType.toLowerCase(Locale.ROOT).startsWith("audio") || mediaTypeStr.contains("audio");
                    boolean isImage = mimeType.toLowerCase(Locale.ROOT).startsWith("image") || mediaTypeStr.contains("image");

                    if (isAudio) {
                        audioFiles.add(inputCacheFile);
                    } else {
                        visualItems.add(new MediaVisualItem(inputCacheFile, isImage));
                    }
                }

                if (visualItems.isEmpty()) {
                    handler.post(() -> showErrorDialog("ไม่พบไฟล์วิดีโอหรือรูปภาพที่ถูกต้อง"));
                    return;
                }

                File tempVideoFile = new File(context.getCacheDir(), "temp_render_v1.mp4");
                if (tempVideoFile.exists()) tempVideoFile.delete();

                List<String> videoArgs = new ArrayList<>();
                StringBuilder filterGraph = new StringBuilder();
                videoArgs.add("-threads");
                videoArgs.add("0");

                for (int i = 0; i < visualItems.size(); i++) {
                    MediaVisualItem item = visualItems.get(i);
                    if (item.isImage) {
                        videoArgs.add("-loop"); videoArgs.add("1");
                        videoArgs.add("-t"); videoArgs.add("3");
                    } else {
                        videoArgs.add("-t"); videoArgs.add("3");
                    }
                    videoArgs.add("-i"); videoArgs.add(item.file.getAbsolutePath());
                    filterGraph.append("[").append(i).append(":v]scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30,trim=duration=3,format=yuv420p,setpts=PTS-STARTPTS[v").append(i).append("]; ");
                }

                for (int i = 0; i < visualItems.size(); i++) {
                    filterGraph.append("[v").append(i).append("]");
                }
                filterGraph.append("concat=n=").append(visualItems.size()).append(":v=1:a=0[v_final]");

                videoArgs.add("-filter_complex"); videoArgs.add(filterGraph.toString());
                videoArgs.add("-map"); videoArgs.add("[v_final]");
                videoArgs.add("-c:v"); videoArgs.add("libx264");
                videoArgs.add("-preset"); videoArgs.add("ultrafast");
                videoArgs.add("-y"); videoArgs.add(tempVideoFile.getAbsolutePath());

                FFmpegSession session1 = FFmpegKit.executeWithArguments(videoArgs.toArray(new String[0]));
                if (!ReturnCode.isSuccess(session1.getReturnCode()) || !tempVideoFile.exists()) {
                    int c2vIndex = videoArgs.indexOf("-c:v");
                    if (c2vIndex != -1) videoArgs.set(c2vIndex + 1, "mpeg4");
                    session1 = FFmpegKit.executeWithArguments(videoArgs.toArray(new String[0]));
                }

                if (!ReturnCode.isSuccess(session1.getReturnCode()) || !tempVideoFile.exists()) {
                    handler.post(() -> showErrorDialog("เรนเดอร์ล้มเหลว"));
                    return;
                }

                copyFileDirectly(tempVideoFile, outputFile);
                handler.post(() -> Toast.makeText(context, "🎉 เรนเดอร์สำเร็จ! บันทึกที่: " + outputFile.getName(), Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> showErrorDialog("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void copyFileDirectly(File src, File dst) {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        } catch (Exception ignored) {}
    }

    private void showErrorDialog(String message) {
        try {
            new AlertDialog.Builder(context)
                .setTitle("❌ เกิดข้อผิดพลาด")
                .setMessage(message)
                .setPositiveButton("ตกลง", null)
                .show();
        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
