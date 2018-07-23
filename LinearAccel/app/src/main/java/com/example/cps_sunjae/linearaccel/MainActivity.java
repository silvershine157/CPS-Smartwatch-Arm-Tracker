package com.example.cps_sunjae.linearaccel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mGravity;
    private Sensor mRot;
    private Sensor rRot;

    TextView AccelView;
    TextView GravView;
    TextView RotView;
    TextView RotView2;
    TextView CalcGravView;

    String gravitySting = "";
    String rotVectorString = "";
    String localGravity = "";

    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mRot = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rRot = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        AccelView  = (TextView) findViewById(R.id.Accel);
        GravView = (TextView) findViewById(R.id.Grav);
        RotView = (TextView) findViewById(R.id.Rot);
        RotView2 = (TextView) findViewById(R.id.Rot2);
        CalcGravView = (TextView) findViewById(R.id.CalcGrav);

        List<Sensor> devicesSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s : devicesSensors) {
            System.out.println(s);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mAccel, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,mGravity,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,mRot,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,rRot,SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
        writeToFile("_gravity.txt", gravitySting);
        writeToFile("_rotVector.txt", rotVectorString);
        writeToFile("_localGravity.txt", localGravity);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            AccelView.setText(String.format("%.3f",event.values[0]) + ", " + String.format("%.3f",event.values[1])
                    + ", " + String.format("%.3f",event.values[2]));
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            GravView.setText(String.format("%.3f",event.values[0]) + ", " + String.format("%.3f",event.values[1])
                    + ", " + String.format("%.3f",event.values[2]));

            gravitySting = gravitySting.concat(event.timestamp + ", " + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2] + "\n");

        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            RotView.setText(String.format("%.3f",event.values[0]) + ", " + String.format("%.3f",event.values[1])
                    + ", " + String.format("%.3f",event.values[2]));

        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            localGravity.concat(event.timestamp + ", ");
            calcGravity(event.values);
            RotView2.setText(String.format("%.3f",event.values[0]) + ", " + String.format("%.3f",event.values[1])
                    + ", " + String.format("%.3f",event.values[2]));

            rotVectorString = rotVectorString.concat(event.timestamp + ", " + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2] + ", " + event.values[3] + "\n");
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
        localGravity = localGravity.concat(result[0] + ", " + result[1] + ", " + result[2] + "\n");

        CalcGravView.setText(String.format("%.3f", result[0]) + " ," + String.format("%.3f", result[1]) + " ," +
                String.format("%.3f", result[2]) + " ,");
    }

    private void hamiltonProduct(float[] x, float[] y, float[] result) {
        result[0] = (x[3]*y[0]) + (x[0]*y[3]) - (x[1]*y[2]) + (x[2]*y[1]);
        result[1] = (x[3]*y[1]) + (x[0]*y[2]) + (x[1]*y[3]) - (x[2]*y[0]);
        result[2] = (x[3]*y[2]) - (x[0]*y[1]) + (x[1]*y[0]) + (x[2]*y[3]);
        result[3] = (x[3]*y[3]) - (x[0]*y[0]) - (x[1]*y[1]) - (x[2]*y[2]);
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

    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
