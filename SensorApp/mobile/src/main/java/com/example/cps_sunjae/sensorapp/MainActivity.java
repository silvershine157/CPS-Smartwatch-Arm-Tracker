package com.example.cps_sunjae.sensorapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Environment;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener{

    private static final String SENSOR_ACCEL = "sensor.accel";
    private static final String SENSOR_GYRO = "sensor.gyro";
    private static final String SENSOR_MAG = "sensor.mag";
    private static final String SENSOR_lACCEL = "sensor.laccel";
    private static final String SENSOR_ROT = "sensor.rot";

    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private File file;

    private String headerAccelX = "\n accel X: ";
    private String headerAccelY = "\n accel Y: ";
    private String headerAccelZ = "\n accel Z: ";

    FileOutputStream outputStream;

    TextView currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentStatus = (TextView) findViewById(R.id.status);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("testdrive", "data changed");
        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String date = df.format(d);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensor") == 0) {
                    Log.d("testdrive", "data received");
                    currentStatus.setText("received data");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    try {
                        String filename = date.concat("_accel.txt");
                        file = new File(path, filename);
                        FileOutputStream f = new FileOutputStream(file);
                        PrintWriter pw = new PrintWriter(f);
                        pw.print(dataMap.getString(SENSOR_ACCEL));
                        pw.flush();
                        f.close();
                        pw.close();

                        filename = date.concat("_gyro.txt");
                        file = new File(path, filename);
                        f = new FileOutputStream(file);
                        pw = new PrintWriter(f);
                        pw.print(dataMap.getString(SENSOR_GYRO));
                        pw.flush();
                        f.close();
                        pw.close();

                        filename = date.concat("_lAccel.txt");
                        file = new File(path, filename);
                        f = new FileOutputStream(file);
                        pw = new PrintWriter(f);
                        pw.print(dataMap.getString(SENSOR_lACCEL));
                        pw.flush();
                        f.close();
                        pw.close();

                        filename = date.concat("_rot.txt");
                        file = new File(path, filename);
                        f = new FileOutputStream(file);
                        pw = new PrintWriter(f);
                        pw.print(dataMap.getString(SENSOR_ROT));
                        pw.flush();
                        f.close();
                        pw.close();

                        filename = date.concat("_mag.txt");
                        file = new File(path, filename);
                        f = new FileOutputStream(file);
                        pw = new PrintWriter(f);
                        pw.print(dataMap.getString(SENSOR_MAG));
                        pw.flush();
                        f.close();
                        pw.close();

                        Log.d("testdrive", "data written");
                        currentStatus.setText("idle");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}
