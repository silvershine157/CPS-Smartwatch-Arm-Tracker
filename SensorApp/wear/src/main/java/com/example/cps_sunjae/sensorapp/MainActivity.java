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
import java.util.List;


public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String SENSOR_ACCEL = "sensor.accel";
    private static final String SENSOR_GYRO = "sensor.gyro";
    private static final String SENSOR_MAG = "sensor.mag";
    private static final String SENSOR_lACCEL = "sensor.laccel";
    private static final String SENSOR_ROT = "sensor.rot";
    private static final String SENSOR_GRAV = "sensor.grav";

    private static final String SENSOR_ORIENT = "sensor.orient";

    // Arrays to hold sensor data
    // Accelerometer
    private ArrayList<Long> accelT = new ArrayList<>();
    private ArrayList<Float> accelX = new ArrayList<>();
    private ArrayList<Float> accelY = new ArrayList<>();
    private ArrayList<Float> accelZ = new ArrayList<>();

    // Magnetometer
    private ArrayList<Long> magT = new ArrayList<>();
    private ArrayList<Float> magX = new ArrayList<>();
    private ArrayList<Float> magY = new ArrayList<>();
    private ArrayList<Float> magZ = new ArrayList<>();

    // Linear Acceleration
    private ArrayList<Long> lAccelT = new ArrayList<>();
    private ArrayList<Float> lAccelX = new ArrayList<>();
    private ArrayList<Float> lAccelY = new ArrayList<>();
    private ArrayList<Float> lAccelZ = new ArrayList<>();

    // Gyroscope
    private ArrayList<Long> gyroT = new ArrayList<>();
    private ArrayList<Float> gyroX = new ArrayList<>();
    private ArrayList<Float> gyroY = new ArrayList<>();
    private ArrayList<Float> gyroZ = new ArrayList<>();

    // Orientation
    private ArrayList<Long> orientT = new ArrayList<>();
    private ArrayList<Float> orientX = new ArrayList<>();
    private ArrayList<Float> orientY = new ArrayList<>();
    private ArrayList<Float> orientZ = new ArrayList<>();

    // Used to calculate orientation
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    // Rotation Vector
    private ArrayList<Long> rotT = new ArrayList<>();
    private ArrayList<Float> rotX = new ArrayList<>();
    private ArrayList<Float> rotY = new ArrayList<>();
    private ArrayList<Float> rotZ = new ArrayList<>();

    // Gravity
    private ArrayList<Long> gravT = new ArrayList<>();
    private ArrayList<Float>gravX = new ArrayList<>();
    private ArrayList<Float>gravY = new ArrayList<>();
    private ArrayList<Float>gravZ = new ArrayList<>();




    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mMag;
    private Sensor mLAccel;
    private Sensor mGyro;
    private Sensor mOrient;
    private Sensor mRot;
    private Sensor mGrav;

    // Sensor variables
    private boolean start = false;
    private String recording = "Recording...";
    private String stop = "Stop";

    // Textview
    TextView currentLabel;

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Enables Always-on
        setAmbientEnabled();

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Create the listeners for each sensor type
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRot = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

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

//        // Check available sensors
//        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//        for (int i = 0; i < deviceSensors.size(); i++) {
//            System.out.println(deviceSensors.get(i));
//        }
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
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                getMagnetometer(event);
            } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                getLinearAcceleration(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                getGyroscope(event);
            } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                getRotationVector(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                getGravity(event);
            }
        }
    }

    private void getAcceleration(SensorEvent event) {
        float accX = event.values[0];
        float accY = event.values[1];
        float accZ = event.values[2];

        // Record the values
        if (start) {
            accelT.add(event.timestamp);
            accelX.add(accX);
            accelY.add(accY);
            accelZ.add(accZ);
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
        }
    }

    private void getMagnetometer(SensorEvent event) {
        float magnetX = event.values[0];
        float magnetY = event.values[1];
        float magnetZ = event.values[2];

        //Record the values
        if (start) {
            magT.add(event.timestamp);
            magX.add(magnetX);
            magY.add(magnetY);
            magZ.add(magnetZ);
            System.arraycopy(event.values,0, mMagnetometerReading, 0, mMagnetometerReading.length);

            // Compute Orientation
            mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
            mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

            orientT.add(event.timestamp);
            orientX.add(mOrientationAngles[0]);
            orientY.add(mOrientationAngles[1]);
            orientZ.add(mOrientationAngles[2]);
        }
    }

    private void getLinearAcceleration(SensorEvent event) {
        float linearAccX = event.values[0];
        float linearAccY = event.values[1];
        float linearAccZ = event.values[2];

        //Record the values
        if (start) {
            lAccelT.add(event.timestamp);
            lAccelX.add(linearAccX);
            lAccelY.add(linearAccY);
            lAccelZ.add(linearAccZ);
        }
    }

    private void getGyroscope(SensorEvent event) {
        float gyX = event.values[0];
        float gyY = event.values[1];
        float gyZ = event.values[2];

        // Record the values
        if (start) {
            gyroT.add(event.timestamp);
            gyroX.add(gyX);
            gyroY.add(gyY);
            gyroZ.add(gyZ);
        }
    }

    private void getRotationVector(SensorEvent event) {
        float rotVecX = event.values[0];
        float rotVecY = event.values[1];
        float rotVecZ = event.values[2];

        // Record the values
        if (start) {
            rotT.add(event.timestamp);
            rotX.add(rotVecX);
            rotY.add(rotVecY);
            rotZ.add(rotVecZ);
        }
    }

    private void getGravity(SensorEvent event) {
        float gravityX = event.values[0];
        float gravityY = event.values[1];
        float gravityZ = event.values[2];

        //Record the values
        if (start) {
            gravT.add(event.timestamp);
            gravX.add(gravityX);
            gravY.add(gravityY);
            gravZ.add(gravityZ);
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
        mSensorManager.registerListener(this, mLAccel, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mRot, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGrav,SensorManager.SENSOR_DELAY_FASTEST);
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
        String accel = "";
        String gyro = "";
        String mag = "";
        String lAccel = "";
        String rot = "";
        String orient = "";
        String grav = "";

        // Accelerometer
        for (int i = 0; i < accelT.size(); i++) {
            accel = accel.concat(Long.toString(accelT.get(i)) + ", " +
                    Float.toString(accelX.get(i)) + ", " +
                    Float.toString(accelY.get(i)) + ", " +
                    Float.toString(accelZ.get(i)) + "\n");
        }

        //GyroScope
        for (int i = 0; i < gyroT.size(); i++) {
            gyro = gyro.concat(Long.toString(gyroT.get(i)) + ", " +
                    Float.toString(gyroX.get(i)) + ", " +
                    Float.toString(gyroY.get(i)) + ", " +
                    Float.toString(gyroZ.get(i)) + "\n");
        }

        //Mag
        for (int i = 0; i < magT.size(); i++) {
            mag = mag.concat(Long.toString(magT.get(i)) + ", " +
                    Float.toString(magX.get(i)) + ", " +
                    Float.toString(magY.get(i)) + ", " +
                    Float.toString(magZ.get(i)) + "\n");

        }

        // Linear Accelerometer
        for (int i = 0; i < lAccelT.size(); i++) {
            lAccel = lAccel.concat(Long.toString(lAccelT.get(i)) + ", " +
                    Float.toString(lAccelX.get(i)) + ", " +
                    Float.toString(lAccelY.get(i)) + ", " +
                    Float.toString(lAccelZ.get(i)) + "\n");
        }

        // Rotation Vector
        for (int i = 0; i < rotT.size(); i++) {
            rot = rot.concat(Long.toString(rotT.get(i)) + ", " +
                    Float.toString(rotX.get(i)) + ", " +
                    Float.toString(rotY.get(i)) + ", " +
                    Float.toString(rotZ.get(i)) + "\n");
        }

        // Gravity
        for (int i = 0; i < gravT.size(); i++) {
            grav = grav.concat(Long.toString(gravT.get(i)) + ", " +
                    Float.toString(gravX.get(i)) + ", " +
                    Float.toString(gravY.get(i)) + ", " +
                    Float.toString(gravZ.get(i)) + "\n");
        }

        // Orientation
        for (int i = 0; i < orientT.size(); i++) {
            orient = orient.concat(Long.toString(orientT.get(i)) + ", " +
                    Float.toString(orientX.get(i)) + ", " +
                    Float.toString(orientY.get(i)) + ", " +
                    Float.toString(orientZ.get(i)) + "\n");
        }

        Log.d("testdrive", "sending data");

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensor");
        putDataMapReq.getDataMap().putString(SENSOR_ACCEL,accel);
        putDataMapReq.getDataMap().putString(SENSOR_GYRO, gyro);
        putDataMapReq.getDataMap().putString(SENSOR_MAG, mag);
        putDataMapReq.getDataMap().putString(SENSOR_lACCEL, lAccel);
        putDataMapReq.getDataMap().putString(SENSOR_ROT, rot);
        putDataMapReq.getDataMap().putString(SENSOR_GRAV, grav);
        putDataMapReq.getDataMap().putString(SENSOR_ORIENT, orient);

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest().setUrgent();

        Task<DataItem> putDataTask = Wearable.getDataClient(this).putDataItem(putDataReq);
        Log.d("testdrive", accel);
        accelT.clear();
        accelX.clear();
        accelY.clear();
        accelZ.clear();
        magT.clear();
        magX.clear();
        magY.clear();
        magZ.clear();
        lAccelT.clear();
        lAccelX.clear();
        lAccelY.clear();
        lAccelZ.clear();
        gyroT.clear();
        gyroX.clear();
        gyroY.clear();
        gyroZ.clear();
        rotT.clear();
        rotX.clear();
        rotY.clear();
        rotZ.clear();
        orientT.clear();
        orientX.clear();
        orientY.clear();
        orientZ.clear();

        Log.d("testdrive", "sent Data");
    }
}

