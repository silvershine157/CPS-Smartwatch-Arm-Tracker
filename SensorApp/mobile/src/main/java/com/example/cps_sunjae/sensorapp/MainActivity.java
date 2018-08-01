package com.example.cps_sunjae.sensorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SensorApp";
    private static final String START_MESSAGE = "start.message";
    private static final String STOP_MESSAGE = "stop.message";

    static TextView currentStatus;
    Button btn_start, btn_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(getApplicationContext(), SensorService.class);
        startService(intent);

        currentStatus = findViewById(R.id.status);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_start.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                btnStart();
            }
        });
        btn_stop.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                btnStop();
            }
        });
        checkPermission();
    }

    public static void makeText(String str) {
        currentStatus.setText(str);
    }

    public static void setColor(final int r, final int g, final int b) {
        currentStatus.setBackgroundColor(Color.rgb(r, g, b));
    }

    private void btnStart() {
        currentStatus.setText("Recording");
        Intent intent = new Intent(START_MESSAGE);
        sendBroadcast(intent);
    }

    private void btnStop() {
        Intent intent = new Intent(STOP_MESSAGE);
        sendBroadcast(intent);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
