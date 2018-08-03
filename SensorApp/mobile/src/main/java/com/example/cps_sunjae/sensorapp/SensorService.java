package com.example.cps_sunjae.sensorapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class SensorService extends Service implements DataClient.OnDataChangedListener {

    private static final String TAG = "SensorAppService";

    private static final String SENSOR_ACCEL = "sensor.accel";
    private static final String SENSOR_lACCEL = "sensor.laccel";
    private static final String SENSOR_GRAV = "sensor.grav";
    private static final String SENSOR_ROTVEC = "sensor.rotvec";

    private static final String START_SENSING_PATH = "/start-sensing";
    private static final String STOP_SENSING_PATH = "/stop-sensing";

    private static final String START_MESSAGE = "start.message";
    private static final String STOP_MESSAGE = "stop.message";

    private static String CHANNEL_ID = "ChannelID";

    private BroadcastReceiver mReceiver;

    Date d;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String date;

    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        Wearable.getDataClient(this).addListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(START_MESSAGE);
        filter.addAction(STOP_MESSAGE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(START_MESSAGE)) {
                    new SendStartTask().execute();
                } else if (intent.getAction().equals(STOP_MESSAGE)) {
                    new SendStopTask().execute();
                }
            }
        };
        this.registerReceiver(mReceiver, filter);

        createNotificationChannel();
        Intent startIntent = new Intent();
        Intent stopIntent = new Intent();
        startIntent.setAction(START_MESSAGE);
        stopIntent.setAction(STOP_MESSAGE);
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0, startIntent, 0);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.noticon)
                .setContentTitle("In Background service")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .addAction(R.drawable.noticon, "start", startPendingIntent)
                .addAction(R.drawable.noticon, "stop", stopPendingIntent);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Log.d(TAG, "onDestroy()");
        Wearable.getDataClient(this).removeListener(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[] {1000, 1000});
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class WriteTask extends AsyncTask<DataMap, Void, Void> {
        @Override
        protected Void doInBackground(DataMap... dataMaps) {
            write(dataMaps[0]);
            return null;
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("testdrive", "data changed");
        // get current time
        d = Calendar.getInstance().getTime();
        date = df.format(d);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensor") == 0) {
                    Log.d("testdrive", "data received");
                    MainActivity.makeText("data received");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    new WriteTask().execute(dataMap);
                    //write(dataMap);
                }
            }
        }
    }

    private void write(DataMap dataMap){

        try {
            loadFromAsset("_accel.txt", dataMap.getAsset(SENSOR_ACCEL));
            loadFromAsset("_grav.txt", dataMap.getAsset(SENSOR_GRAV));
            loadFromAsset("_lAccel.txt", dataMap.getAsset(SENSOR_lACCEL));
            loadFromAsset("_rotVector.txt", dataMap.getAsset(SENSOR_ROTVEC));
            Log.d("testdrive", "data written");
            MainActivity.makeText("data written");
            Random rand = new Random();
            int r = rand.nextInt(256);
            int g = rand.nextInt(256);
            int b = rand.nextInt(256);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.noticon)
                    .setContentTitle("Data Written")
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setColor(Color.rgb(r, g, b));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                builder.setPriority(NotificationCompat.PRIORITY_MAX);
            }
            Notification notification = builder.build();
            NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(2, notification);
            MainActivity.setColor(r, g, b);
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

    private void writeToFile(String type, String data) {

        // make new File
        File dir = new File(path, date);
        dir.mkdirs();
        String filename = date + "/" + date + type;
        File file = new File(path, filename);
        if (file.isFile()) {
            filename = date + "/" + date + "_2" + type;
            file = new File(path, filename);
        }
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

    public class SendStartTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartMessage(node);
            }
            return null;
        }
    }

    public class SendStopTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStopMessage(node);
            }
            return null;
        }
    }

    public void sendStartMessage(String node) {
        Wearable.getMessageClient(this).sendMessage(node, START_SENSING_PATH, new byte[0]);
    }

    public void sendStopMessage(String node) {
        Wearable.getMessageClient(this).sendMessage(node, STOP_SENSING_PATH, new byte[0]);
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
}
