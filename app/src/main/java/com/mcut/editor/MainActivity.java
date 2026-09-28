package com.mcut.editor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_MEDIA_REQUEST = 2002;
    private VideoProcessor videoProcessor;
    private final List<MediaItem> timelineList = new ArrayList<>();
    private CustomVideoView videoPreview;
    private LinearLayout containerMediaList;
    private String currentSelectedStyle = "Free Fire Highlight";

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

        View.OnClickListener styleListener = v -> {
            Button b = (Button) v;
            currentSelectedStyle = b.getText().toString();
            videoProcessor.processVideoWithStyle(currentSelectedStyle, timelineList);
        };

        btnStyleFreeFire.setOnClickListener(styleListener);
        btnStyleVlog.setOnClickListener(styleListener);
        btnStyleStory.setOnClickListener(styleListener);
        btnStyleAds.setOnClickListener(styleListener);

        btnAddMedia.setOnClickListener(v -> openFilePicker());
        btnTestEngine.setOnClickListener(v -> videoProcessor.testFFmpegConnection());
        btnSaveRender.setOnClickListener(v -> videoProcessor.saveGeneratedVideo(timelineList));
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
        String uriStr = uri.toString();
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "";
        timelineList.add(new MediaItem(uriStr, mimeType));
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
