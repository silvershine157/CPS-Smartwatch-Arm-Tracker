package com.example.cps_sunjae.sensorapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class SensorService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                if (path.equals("/sensorData")) {
//                    long time = dataMap.getLong("timestamp");
                    float accelX = dataMap.getFloat("accelX");
                    float accelY = dataMap.getFloat("accelY");
                    float accelZ = dataMap.getFloat("accelZ");

                    Intent intent = new Intent();
                    intent.setAction("GET_DATA");
//                    intent.putExtra("timestamp", time);
                    intent.putExtra("accelX", accelX);
                    intent.putExtra("accelY", accelY);
                    intent.putExtra("accelZ", accelZ);
                    sendBroadcast(intent);
                }
            }
        }
    }
}
