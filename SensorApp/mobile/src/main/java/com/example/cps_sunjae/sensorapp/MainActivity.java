package com.example.cps_sunjae.sensorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
    Button btn_start, btn_stop, btn_file;

    AudioTrack audioTrack;

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
        btn_file = findViewById(R.id.btn_file);
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
        btn_file.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FileActivity.class));
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
        //playSound();
    }

    private void btnStop() {
        Intent intent = new Intent(STOP_MESSAGE);
        sendBroadcast(intent);
        //stopSound();
    }

    private void playSound() {
        if (audioTrack != null) {
            return;
        }
        double temp;
        int hz = 19000;
        int sampleRate = 48000;
        short sound[] = new short[sampleRate];

        for (int i = 0; i < sampleRate; i++) {
            temp = Math.sin(2 * Math.PI * i / sampleRate * hz);
            sound[i] = (short) (temp * 10000);
        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, sampleRate, AudioTrack.MODE_STATIC);
        audioTrack.write(sound, 0, sampleRate);
        audioTrack.setLoopPoints(0, sampleRate/4, -1);
        audioTrack.play();
    }

    private void stopSound() {
        if (audioTrack == null) {
            return;
        }
        audioTrack.pause();
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 0);
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
