package com.example.cps_sunjae.sensorapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Environment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final String SENSOR_GRAV = "sensor.grav";
    private static final String SENSOR_ORIENT = "sensor.orient";

    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private File file;

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

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensor") == 0) {
                    Log.d("testdrive", "data received");
                    currentStatus.setText("received data");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    try {
                        loadFromAsset("_accel.txt", dataMap.getAsset(SENSOR_ACCEL));
                        loadFromAsset("_gyro.txt", dataMap.getAsset(SENSOR_GYRO));
                        loadFromAsset("_mag.txt", dataMap.getAsset(SENSOR_MAG));
//                        loadFromAsset("_lAccel.txt", dataMap.getAsset(SENSOR_lACCEL));
//                        loadFromAsset("_rot.txt", dataMap.getAsset(SENSOR_ROT));
//                        loadFromAsset("_grav.txt", dataMap.getAsset(SENSOR_GRAV));
//                        loadFromAsset("_orient.txt", dataMap.getAsset(SENSOR_ORIENT));

                        Log.d("testdrive", "data written");
                        currentStatus.setText("idle");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private void writeToFile(String type, String data) {
        // get current time
        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String date = df.format(d);

        // make new File
        String filename = date.concat(type);
        File file = new File(path, filename);
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.print(data);
            pw.flush();
            f.close();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadFromAsset(final String fileType, Asset asset) {

        if (asset == null) {
            throw new IllegalArgumentException("Aset must be non-null");
        }

        try {
            Task<DataClient.GetFdForAssetResponse> task = Wearable.getDataClient(this).getFdForAsset(asset);
            task.addOnSuccessListener(new OnSuccessListener<DataClient.GetFdForAssetResponse>() {
                @Override
                public void onSuccess(DataClient.GetFdForAssetResponse response) {
                    InputStream assetInputStream = response.getInputStream();
                    if (assetInputStream == null) {
                        Log.v("testdrive", "Requestted an unkown Asset.");
                        return;
                    }

                    BufferedReader r = new BufferedReader(new InputStreamReader(assetInputStream));
                    StringBuilder str = new StringBuilder();
                    String line;
                    try {
                        while ((line = r.readLine()) != null) {
                            str.append(line).append('\n');
                        }
                        writeToFile(fileType, str.toString());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
