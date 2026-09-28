package com.mcut.editor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_MEDIA_REQUEST = 2002;
    private VideoProcessor videoProcessor;
    private final List<MediaItem> timelineList = new ArrayList<>();
    private CustomVideoView videoPreview;
    private LinearLayout containerMediaList;
    private String currentSelectedStyle = "Free Fire Highlight"; // ค่าเริ่มต้น

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoProcessor = new VideoProcessor(this);
        videoPreview = findViewById(R.id.videoPreview);
        containerMediaList = findViewById(R.id.containerMediaList);

        Button btnAddMedia = findViewById(R.id.btnAddMedia);
        Button btnTestEngine = findViewById(R.id.btnTestEngine);
        Button btnSaveRender = findViewById(R.id.btnSaveRender);

        Button btnStyleFreeFire = findViewById(R.id.btnStyleFreeFire);
        Button btnStyleVlog = findViewById(R.id.btnStyleVlog);
        Button btnStyleStory = findViewById(R.id.btnStyleStory);
        Button btnStyleAds = findViewById(R.id.btnStyleAds);

        // เปลี่ยนให้ปุ่มเลือกสไตล์ ทำหน้าที่ "แค่เลือกและบันทึกค่าไว้" ยังไม่เรนเดอร์
        View.OnClickListener styleListener = v -> {
            Button b = (Button) v;
            currentSelectedStyle = b.getText().toString();
            Toast.makeText(this, "เลือกสไตล์: " + currentSelectedStyle + " (กดปุ่ม Export เพื่อเริ่มตัดต่อ)", Toast.LENGTH_SHORT).show();
        };

        btnStyleFreeFire.setOnClickListener(styleListener);
        btnStyleVlog.setOnClickListener(styleListener);
        btnStyleStory.setOnClickListener(styleListener);
        btnStyleAds.setOnClickListener(styleListener);

        btnAddMedia.setOnClickListener(v -> openFilePicker());
        btnTestEngine.setOnClickListener(v -> videoProcessor.testFFmpegConnection());
        
        // ย้ายการประมวลผลและการเรนเดอร์ทั้งหมดมาไว้ที่ปุ่ม EXPORT ตัวนี้!
        btnSaveRender.setOnClickListener(v -> startVideoProcessingWithStyle());
    }

    // --- ฟังก์ชันสั่งประมวลผลและตัดต่อ (ทำงานเมื่อกดปุ่ม EXPORT เท่านั้น) ---
    private void startVideoProcessingWithStyle() {
        if (timelineList.isEmpty()) {
            Toast.makeText(this, "กรุณาเพิ่มไฟล์วิดีโอก่อนกด Export!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ดึงไฟล์วิดีโอตัวแรกจากไทม์ไลน์
        MediaItem firstItem = timelineList.get(0);
        Uri inputUri = null;
        if (firstItem.localConfiguration != null) {
            inputUri = firstItem.localConfiguration.uri;
        }

        if (inputUri == null) {
            Toast.makeText(this, "ไม่พบที่อยู่ของไฟล์มีเดีย!", Toast.LENGTH_SHORT).show();
            return;
        }

        // กำหนดไฟล์ขาออก (Output File) ไปที่โฟลเดอร์ Movies ของเครื่อง
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File outputPath = new File(exportDir, "MCut_Output_" + System.currentTimeMillis() + ".mp4");

        Toast.makeText(this, "กำลังเริ่มตัดต่อสไตล์ " + currentSelectedStyle + "...", Toast.LENGTH_LONG).show();

        // ส่งข้อมูลเข้า VideoProcessor เพื่อทำการแปลงและเรนเดอร์จริง
        videoProcessor.processVideoWithStyle(
                currentSelectedStyle,
                timelineList,
                inputUri,
                outputPath,
                new VideoProcessor.VideoCallback() {
                    @Override
                    public void onSuccess(File outputFile) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "ตัดต่อสำเร็จ! บันทึกที่โฟลเดอร์ Movies: " + outputFile.getName(), Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "เกิดข้อผิดพลาด: " + errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "เลือกไฟล์มีเดีย"), PICK_MEDIA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    addUriToTimeline(uri);
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                addUriToTimeline(uri);
            }
            updateTimelineUI();
        }
    }

    private void addUriToTimeline(Uri uri) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        timelineList.add(mediaItem);
    }

    private void updateTimelineUI() {
        containerMediaList.removeAllViews();
        for (int i = 0; i < timelineList.size(); i++) {
            TextView itemTv = new TextView(this);
            itemTv.setText("Clip #" + (i + 1));
            itemTv.setTextColor(0xFFFFFFFF);
            itemTv.setPadding(20, 14, 20, 14);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(6, 0, 6, 0);
            itemTv.setLayoutParams(params);
            itemTv.setBackgroundColor(0xFF334155);

            containerMediaList.addView(itemTv);
        }
        Toast.makeText(this, "อัปเดตไทม์ไลน์แล้ว: " + timelineList.size() + " ไฟล์", Toast.LENGTH_SHORT).show();
    }
}
