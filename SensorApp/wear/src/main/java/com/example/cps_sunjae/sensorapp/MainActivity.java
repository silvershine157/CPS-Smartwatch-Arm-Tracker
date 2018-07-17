package com.example.cps_sunjae.sensorapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String SENSOR_ACCEL = "sensor.accel";
    private static final String SENSOR_GYRO = "sensor.gyro";
    private static final String SENSOR_ORIENT = "sensor.orient";
    // CSV files
    private BufferedWriter bw = null;

    private String filename = "sensor_data.txt";
    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private File file;
    FileOutputStream outputStream;
    private DataClient mDataClient;

    // Arrays to hold sensor data
    private ArrayList<Float> accelX = new ArrayList<>();
    private ArrayList<Float> accelY = new ArrayList<>();
    private ArrayList<Float> accelZ = new ArrayList<>();
    private ArrayList<Float> gyroX = new ArrayList<>();
    private ArrayList<Float> gyroY = new ArrayList<>();
    private ArrayList<Float> gyroZ = new ArrayList<>();

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mGyro;

    // Sensor variables
    private boolean start = false;
    private String recording = "Recording...";
    private String stop = "Stop";

    // Textview
    TextView currentLabel;

    //Veryify storage permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Enables Always-on
        setAmbientEnabled();

        file = new File(path, filename);

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Create the listeners for each sensor type
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Set Labels
        currentLabel = (TextView) findViewById(R.id.currentActivity);

        // Set Buttons
        findViewById(R.id.btn_start).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        start = true;
                        currentLabel.setText(recording);
                    }
                }
        );
        findViewById(R.id.btn_stop).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        start = false;
                        currentLabel.setText(stop);
                        sendSensorData();
                    }
                }
        );
    }

    /**
     * Called each time the sensor changes. Fills buffers to be written to the CSV.
     *
     * @param event The event corresponding to a sensor
     */
    public void onSensorChanged(SensorEvent event) {
        if (start) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getAcceleration(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                getGyroscope(event);
            }
        }
    }

    private void getAcceleration(SensorEvent event) {
        float accX = event.values[0];
        float accY = event.values[1];
        float accZ = event.values[2];

        // Record the values
        if (start) {
            accelX.add(accX);
            accelY.add(accY);
            accelZ.add(accZ);
        }
    }

    private void getGyroscope(SensorEvent event) {
        float gyX = event.values[0];
        float gyY = event.values[1];
        float gyZ = event.values[2];

        // Record the values
        if (start) {
            gyroX.add(gyX);
            gyroY.add(gyY);
            gyroZ.add(gyZ);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    /**
     * Make CSV file and send to phone
     */
    public void sendSensorData(){
        try{
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String accel = "";
        String gyro = "";
        String orient = "";
        //accel
        for (int i = 0; i < accelX.size(); i++) {
            accel = accel.concat(Float.toString(accelX.get(i)) + ", " +
                    Float.toString(accelY.get(i)) + ", " +
                    Float.toString(accelZ.get(i)) + "\n");
            try {
                outputStream.write(accel.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < gyroX.size(); i++) {
            gyro = gyro.concat(Float.toString(gyroX.get(i)) + ", " +
                    Float.toString(gyroY.get(i)) + ", " +
                    Float.toString(gyroZ.get(i)) + "\n");
        }
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensor");
        putDataMapReq.getDataMap().putString(SENSOR_ACCEL,accel);
        putDataMapReq.getDataMap().putString(SENSOR_GYRO, gyro);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        Task<DataItem> putDataTask = mDataClient.putDataItem(putDataReq);
    }
}

