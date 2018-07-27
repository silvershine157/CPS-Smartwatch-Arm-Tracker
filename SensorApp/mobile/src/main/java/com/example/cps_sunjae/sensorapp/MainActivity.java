package com.example.cps_sunjae.sensorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Environment;
import android.widget.Toast;

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
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener{

    private static final String TAG = "SensorApp";
    private static final String START_SENSING_PATH = "/start-sensing";
    private static final String STOP_SENSING_PATH = "/stop-sensing";

    private static final String SENSOR_ACCEL = "sensor.accel";
    private static final String SENSOR_GYRO = "sensor.gyro";
    private static final String SENSOR_MAG = "sensor.mag";
    private static final String SENSOR_lACCEL = "sensor.laccel";
    private static final String SENSOR_GRAV = "sensor.grav";
    private static final String SENSOR_ROTVEC = "sensor.rotvec";
    private static final String SENSOR_ORIENT = "sensor.orient";

    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    TextView currentStatus;
    Button btn, btn_dwn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentStatus = (TextView) findViewById(R.id.status);
        btn = findViewById(R.id.btn);
        btn_dwn = findViewById(R.id.btn_downloads);
        btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                btnClick();
            }
        });
        btn_dwn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String pkg = "com.android.documentsui";
                if (checkPackage(pkg)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "no package");
                    Toast.makeText(getApplicationContext(), "Need " + pkg, Toast.LENGTH_LONG).show();
                }
            }
        });
        checkPermission();
    }

    public boolean checkPackage(String pkg) {
        boolean isExist = false;
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if (mApps.get(i).activityInfo.packageName.equals(pkg)) {
                    isExist = true;
                    break;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return isExist;
    }

    private void btnClick(){
        new SendTask().execute();
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
        try {
            List<Node> nodes = Tasks.await(nodeListTask);
            for (Node node : nodes) {
                results.add(node.getId());
            }
        } catch (Exception e) {
            Log.d(TAG, "E: " + e);
        }

        return results;
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendMessage(node);
            }
            return null;
        }
    }

    public void sendMessage(String node) {
        if (btn.getText().equals("start")) {
            Wearable.getMessageClient(this).sendMessage(node, START_SENSING_PATH, new byte[0]);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setText("stop");
                    currentStatus.setText("Recording");
                }
            });
        } else {
            Wearable.getMessageClient(this).sendMessage(node, STOP_SENSING_PATH, new byte[0]);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setText("start");
                }
            });
        }
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
                        loadFromAsset("_grav.txt", dataMap.getAsset(SENSOR_GRAV));
                        loadFromAsset("_lAccel.txt", dataMap.getAsset(SENSOR_lACCEL));
                        loadFromAsset("_rotVector.txt", dataMap.getAsset(SENSOR_ROTVEC));
                        loadFromAsset("_orient.txt", dataMap.getAsset(SENSOR_ORIENT));

                        Log.d("testdrive", "data written");
                        currentStatus.setText("data written");
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
        File dir = new File(path, date);
        dir.mkdirs();
        String filename = date + "/" + date + type;
        File file = new File(path, filename);
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.print(data);
            pw.flush();
            f.close();
            pw.close();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
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
