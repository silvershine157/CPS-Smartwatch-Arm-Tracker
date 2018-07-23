package com.example.cps_sunjae.sensorapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
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


public class MainActivity extends WearableActivity implements SensorEventListener,
        MessageClient.OnMessageReceivedListener {

    private static final int CALIBRATE_MAX = 20;

    private static final String TAG = "SensorAppW";
    private static final String START_SENSING_PATH = "/start-sensing";
    private static final String STOP_SENSING_PATH = "/stop-sensing";

    private static final String SENSOR_ACCEL = "sensor.accel";
    private static final String SENSOR_GYRO = "sensor.gyro";
    private static final String SENSOR_MAG = "sensor.mag";
    private static final String SENSOR_lACCEL = "sensor.laccel";
    private static final String SENSOR_GRAV = "sensor.grav";
    private static final String SENSOR_ROTVEC = "sensor.rotvec";

    private static final int SENSING_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
//    private static final int SENSING_DELAY = 20000;

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

    // Used to calculate mean gravity
    private float[] gravitySum = {0,0,0};
    private float gravityMean = 0;
    private int calibrateCounter = 0;

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
    private Sensor mGyro;
    private Sensor gRot;
    private Sensor mOrient;
    private Sensor mRot;
    private Sensor mGrav;

    // Sensor variables
    private boolean start = false;
    private String recording = "Recording...";
    private String stop = "Stop";

    // Textview
    TextView currentLabel;

    GoogleApiClient mGoogleApiClient;

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
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRot = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gRot = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        // Set Labels
        currentLabel = (TextView) findViewById(R.id.currentActivity);

        // Set Buttons
        findViewById(R.id.btn_start).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        startSensing();
                    }
                }
        );
        findViewById(R.id.btn_stop).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        stopSensing();
                    }
                }
        );
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //Log.d(TAG, "onMessageReceived: " + messageEvent);
        if (messageEvent.getPath().equals(START_SENSING_PATH)) {
            startSensing();
        } else if (messageEvent.getPath().equals(STOP_SENSING_PATH)) {
            stopSensing();
        }
    }

    public void startSensing() {
        if (!start) {
            start = true;

            mSensorManager.registerListener(this, mAccel, SENSING_DELAY);
            mSensorManager.registerListener(this, mGyro, SENSING_DELAY);
            mSensorManager.registerListener(this, mMag, SENSING_DELAY);
            mSensorManager.registerListener(this,gRot, SENSING_DELAY);

//            mSensorManager.registerListener(this, mLAccel, SENSING_DELAY);
            mSensorManager.registerListener(this, mRot, SENSING_DELAY);
//            mSensorManager.registerListener(this, mGrav, SENSING_DELAY);
        }
    }

    public void stopSensing() {
        if (start) {
            start = false;

            mSensorManager.unregisterListener(this);
            sendSensorData();

            currentLabel.setText(stop);
        }
    }

    /**
     * Called each time the sensor changes. Fills buffers to be written to the CSV.
     *
     * @param event The event corresponding to a sensor
     */
    public void onSensorChanged(SensorEvent event) {
        if (start) {
//            currentLabel.setText(recording);

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getAcceleration(event);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                getMagnetometer(event);
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

        if (calibrateCounter < CALIBRATE_MAX) {
            gravitySum[0] += accX;
            gravitySum[1] += accY;
            gravitySum[2] += accZ;
            calibrateCounter ++;
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
//            mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
//            mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

            orientT.add(event.timestamp);
            orientX.add(mOrientationAngles[0]);
            orientY.add(mOrientationAngles[1]);
            orientZ.add(mOrientationAngles[2]);
        }
    }

    private void getLinearAcceleration(float[] rotVec, float[] accel) {
        float[] globalAcc = localToGlobal(rotVec, accel);
        lAccelX.add(globalAcc[0]);
        lAccelY.add(globalAcc[1]);
        lAccelZ.add(globalAcc[2] - gravityMean);

        currentLabel.setText(String.format("%.3f", globalAcc[0]) + " ," + String.format("%.3f", globalAcc[1]) + " ," +
        String.format("%.3f", globalAcc[2] - gravityMean));

//        Log.v("testdrive", String.valueOf(gravityMean));
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

//            currentLabel.setText(String.format("%.3f", rotVecX) + " ," + String.format("%.3f", rotVecY) + " ," +
//                    String.format("%.3f", rotVecZ) + " ,");

            gravT.add(event.timestamp);
            calcGravity(event.values);

            if (calibrateCounter >= CALIBRATE_MAX) {
                if (gravityMean == 0) {
                    float[] meanLocalGravity =
                            {gravitySum[0]/CALIBRATE_MAX, gravitySum[1]/CALIBRATE_MAX, gravitySum[2]/CALIBRATE_MAX, 0};

                    float[] globalGrav = localToGlobal(event.values, meanLocalGravity);

                    gravityMean = globalGrav[2];
                    Log.v("testdrive", String.valueOf(gravityMean));
                }

                lAccelT.add(event.timestamp);
                getLinearAcceleration(event.values,
                new float[]{accelX.get(accelT.size()-1),accelY.get(accelT.size()-1),accelZ.get(accelT.size()-1), 0});
            }
//            if (gravityMean == 0) {
//                float[] globalGrav = localToGlobal(event.values,
//                        new float[]{accelX.get(accelT.size()-1),accelY.get(accelT.size()-1),accelZ.get(accelT.size()-1), 0});
//
//                if (globalGrav[2]>=9) {
//                    gravitySet.add(globalGrav[2]);
//                }
//
//
//                float gravitySum = 0;
//                if (gravitySet.size() >= 20) {
//                    for(float g : gravitySet) {
//                        Log.v("testdrive", String.valueOf(g));
//                        gravitySum += g;
//                    }
//
//                    gravityMean = gravitySum / gravitySet.size();
//                }
//            } else {
//                lAccelT.add(event.timestamp);
//                getLinearAcceleration(event.values,
//                        new float[]{accelX.get(accelT.size()-1),accelY.get(accelT.size()-1),accelZ.get(accelT.size()-1), 0});
//            }
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

    private void calcGravity(float[] rotVector) {
        float[] gravity = {0,0,(float)9.81, 0};
        float temp[] = new float[4];
        float result[] = new float[4];

        float[] rotVectorInverse = new float[4];
        float inverseDenom = (float)(Math.pow(rotVector[3],2) + Math.pow(rotVector[0], 2)
                + Math.pow(rotVector[1], 2) + Math.pow(rotVector[2], 2));
        rotVectorInverse[0] = (-1)*rotVector[0] / inverseDenom;
        rotVectorInverse[1] = (-1)*rotVector[1] / inverseDenom;
        rotVectorInverse[2] = (-1)*rotVector[2] / inverseDenom;
        rotVectorInverse[3] = rotVector[3] / inverseDenom;

        hamiltonProduct(rotVector, gravity, temp);
        hamiltonProduct(temp, rotVectorInverse, result);

        gravX.add(result[0]);
        gravY.add(result[1]);
        gravZ.add(result[2]);

//        currentLabel.setText(String.format("%.3f", result[0]) + " ," + String.format("%.3f", result[1]) + " ," +
//                String.format("%.3f", result[2]) + " ,");
    }

    private float[] localToGlobal(float[] rotVector, float[] accel) {
        float temp[] = new float[4];
        float result[] = new float[4];

        float[] rotVectorInverse = new float[4];
        float inverseDenom = (float)(Math.pow(rotVector[3],2) + Math.pow(rotVector[0], 2)
                + Math.pow(rotVector[1], 2) + Math.pow(rotVector[2], 2));
        rotVectorInverse[0] = (-1)*rotVector[0] / inverseDenom;
        rotVectorInverse[1] = (-1)*rotVector[1] / inverseDenom;
        rotVectorInverse[2] = (-1)*rotVector[2] / inverseDenom;
        rotVectorInverse[3] = rotVector[3] / inverseDenom;

        hamiltonProduct(rotVectorInverse, accel, temp);
        hamiltonProduct(temp, rotVector, result);

        return new float[]{result[0], result[1], result[2]};
    }

    private void hamiltonProduct(float[] x, float[] y, float[] result) {
        result[0] = (x[3]*y[0]) + (x[0]*y[3]) - (x[1]*y[2]) + (x[2]*y[1]);
        result[1] = (x[3]*y[1]) + (x[0]*y[2]) + (x[1]*y[3]) - (x[2]*y[0]);
        result[2] = (x[3]*y[2]) - (x[0]*y[1]) + (x[1]*y[0]) + (x[2]*y[3]);
        result[3] = (x[3]*y[3]) - (x[0]*y[0]) - (x[1]*y[1]) - (x[2]*y[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
    }

    @Override
    protected void onResume() {
        super.onResume();

        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensing();
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
            accel = accel.concat(Long.toString(accelT.get(i)) + "\t" +
                    Float.toString(accelX.get(i)) + "\t" +
                    Float.toString(accelY.get(i)) + "\t" +
                    Float.toString(accelZ.get(i)) + "\n");
        }

        //GyroScope
        for (int i = 0; i < gyroT.size(); i++) {
            gyro = gyro.concat(Long.toString(gyroT.get(i)) + "\t" +
                    Float.toString(gyroX.get(i)) + "\t" +
                    Float.toString(gyroY.get(i)) + "\t" +
                    Float.toString(gyroZ.get(i)) + "\n");
        }

        //Mag
        for (int i = 0; i < magT.size(); i++) {
            mag = mag.concat(Long.toString(magT.get(i)) + "\t" +
                    Float.toString(magX.get(i)) + "\t" +
                    Float.toString(magY.get(i)) + "\t" +
                    Float.toString(magZ.get(i)) + "\n");

        }

        // Linear Accelerometer
        for (int i = 0; i < lAccelT.size(); i++) {
            lAccel = lAccel.concat(Long.toString(lAccelT.get(i)) + "\t" +
                    Float.toString(lAccelX.get(i)) + "\t" +
                    Float.toString(lAccelY.get(i)) + "\t" +
                    Float.toString(lAccelZ.get(i)) + "\n");
        }

        // Rotation Vector
        for (int i = 0; i < rotT.size(); i++) {
            rot = rot.concat(Long.toString(rotT.get(i)) + "\t" +
                    Float.toString(rotX.get(i)) + "\t" +
                    Float.toString(rotY.get(i)) + "\t" +
                    Float.toString(rotZ.get(i)) + "\n");
        }

        // Gravity
        for (int i = 0; i < gravT.size(); i++) {
            grav = grav.concat(Long.toString(gravT.get(i)) + "\t" +
                    Float.toString(gravX.get(i)) + "\t" +
                    Float.toString(gravY.get(i)) + "\t" +
                    Float.toString(gravZ.get(i)) + "\n");
        }
//
//        // Orientation
//        for (int i = 0; i < orientT.size(); i++) {
//            orient = orient.concat(Long.toString(orientT.get(i)) + "\t" +
//                    Float.toString(orientX.get(i)) + "\t" +
//                    Float.toString(orientY.get(i)) + "\t" +
//                    Float.toString(orientZ.get(i)) + "\n");
//        }
//        System.out.println(orient);

        Log.d("testdrive", "sending data");

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensor");
        Asset accelAsset = Asset.createFromBytes(accel.getBytes());
        Asset gyroAsset = Asset.createFromBytes(gyro.getBytes());
        Asset magAsset = Asset.createFromBytes(mag.getBytes());
        Asset gravAsset = Asset.createFromBytes(grav.getBytes());
        Asset lAccelAsset = Asset.createFromBytes(lAccel.getBytes());
        Asset rotVecAsset = Asset.createFromBytes(rot.getBytes());

        putDataMapReq.getDataMap().putAsset(SENSOR_ACCEL, accelAsset);
        putDataMapReq.getDataMap().putAsset(SENSOR_GYRO, gyroAsset);
        putDataMapReq.getDataMap().putAsset(SENSOR_MAG,magAsset);
        putDataMapReq.getDataMap().putAsset(SENSOR_GRAV,gravAsset);
        putDataMapReq.getDataMap().putAsset(SENSOR_lACCEL,lAccelAsset);
        putDataMapReq.getDataMap().putAsset(SENSOR_ROTVEC,rotVecAsset);



        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest().setUrgent();

        Task<DataItem> putDataTask = Wearable.getDataClient(this).putDataItem(putDataReq);
//        Log.d("testdrive", accel);
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

