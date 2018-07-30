package com.example.cps_sunjae.sensorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
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
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SensorApp";
    private static final String START_SENSING_PATH = "/start-sensing";
    private static final String STOP_SENSING_PATH = "/stop-sensing";

    static TextView currentStatus;
    Button btn, btn_dwn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(getApplicationContext(), SensorService.class);
        startService(intent);

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

    public static void makeText(String str) {
        currentStatus.setText(str);
    }

    public static void setColor(int r, int g, int b) {
        currentStatus.setBackgroundColor(Color.rgb(r, g, b));
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
