package com.example.cps_sunjae.sensorapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String filename = "sensor_data.txt";
    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private File file;

    private String headerAccelX = "\n accel X: ";
    private String headerAccelY = "\n accel Y: ";
    private String headerAccelZ = "\n accel Z: ";

    FileOutputStream outputStream;
    ArrayList accelXArray;
    ArrayList accelYArray;
    ArrayList accelZArray;

    private TextView mAccelX;
    private TextView mAccelY;
    private TextView mAccelZ;

    BroadcastReceiver dataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccelX = (TextView) findViewById(R.id.accelX);
        mAccelY = (TextView) findViewById(R.id.accelY);
        mAccelZ = (TextView) findViewById(R.id.accelZ);

        accelXArray = new ArrayList();
        accelYArray = new ArrayList();
        accelZArray = new ArrayList();


        file = new File(path, filename);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("GET_DATA");
        try{
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    accelXArray.add(intent.getFloatExtra("accelX", 0));
                    accelYArray.add(intent.getFloatExtra("accelY", 0));
                    accelZArray.add(intent.getFloatExtra("accelZ", 0));

                    if (accelXArray.size() >= 50) {
                        try {
                            for (int i = 0; i < accelXArray.size(); i++ ) {
                                Log.d("sunjae", "writing");
                                outputStream.write(headerAccelX.getBytes());
                                outputStream.write(String.valueOf(accelXArray.get(i)).getBytes());
                                outputStream.write(headerAccelY.getBytes());
                                outputStream.write(String.valueOf(accelYArray.get(i)).getBytes());
                                outputStream.write(headerAccelZ.getBytes());
                                outputStream.write(String.valueOf(accelZArray.get(i)).getBytes());
                            }
                            accelXArray.clear();
                            accelYArray.clear();
                            accelZArray.clear();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }
            }
        };
        registerReceiver(dataReceiver, filter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(dataReceiver);
        super.onStop();
    }

}
