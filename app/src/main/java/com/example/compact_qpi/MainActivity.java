package com.example.compact_qpi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.compact_qpi.video_acticity.VideoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1_main);

        // 바로 VideoActivitiy로 넘어가자
        Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
        startActivity(intent);
    }
}