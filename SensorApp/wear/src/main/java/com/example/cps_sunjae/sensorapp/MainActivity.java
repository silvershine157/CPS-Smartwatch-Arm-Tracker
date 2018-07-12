package com.example.cps_sunjae.sensorapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.w3c.dom.Text;


public class MainActivity extends WearableActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private TextView mAccelX;
    private TextView mAccelY;
    private TextView mAccelZ;
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mGyro;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccelX = (TextView) findViewById(R.id.accelX);
        mAccelY = (TextView) findViewById(R.id.accelY);
        mAccelZ = (TextView) findViewById(R.id.accelZ);

        // Enables Always-on
        setAmbientEnabled();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void sendSensorData(float accelX, float accelY, float accelZ) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/sensorData");

//        putDataMapRequest.getDataMap().putLong("timestamp", timestamp);
        putDataMapRequest.getDataMap().putFloat("accelX", accelX);
        putDataMapRequest.getDataMap().putFloat("accelY", accelY);
        putDataMapRequest.getDataMap().putFloat("accelZ", accelZ);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.d("sunjae", "send Successful");
                        } else {

                        }
                    }
                });
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float accelX = event.values[0];
        float accelY = event.values[1];
        float accelZ = event.values[2];


        mAccelX.setText(String.valueOf(accelX) + "\n");
        mAccelY.setText(String.valueOf(accelY) + "\n");
        mAccelZ.setText(String.valueOf(accelZ));
        sendSensorData(accelX, accelY, accelZ);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mAccel,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
