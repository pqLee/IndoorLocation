package com.intel.rssmagdetect.model;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.intel.rssmagdetect.ui.MainActivity;

import java.util.ArrayList;

/**
 * @author 1056550980@qq.com
 * Created by lpq on 17-2-22.
 * @description 传感器数据的处理，地磁、方向、加速度、陀螺仪、重力.
 */

public class SensorsDataManager {
    private SensorManager sensorManager;
    private Sensor msensor;
    private Sensor osensor;
    private Sensor asensor;
    private Sensor gsensor;
    private Sensor grasensor;
    private SensorEventListener mSensorListener;
    private SensorEventListener oSensorListener;
    private SensorEventListener aSensorListener;
    private SensorEventListener gSensorListener;
    private SensorEventListener graSensorListener;

    private float[] temp_m = new float[3];
    private float[] temp_o = new float[3];
    private float[] temp_a = new float[3];
    private float[] temp_g = new float[3];
    private float[] temp_gra = new float[3];

    public ArrayList<ArrayList<Integer>> dataMagnetic = new ArrayList<ArrayList<Integer>>(3);
    public ArrayList<ArrayList<Integer>> dataOrientation = new ArrayList<ArrayList<Integer>>(3);
    public ArrayList<ArrayList<Integer>> dataAccelerate = new ArrayList<ArrayList<Integer>>(3);
    public ArrayList<ArrayList<Integer>> dataGyroscope = new ArrayList<ArrayList<Integer>>(3);
    public ArrayList<ArrayList<Integer>> dataGravity = new ArrayList<ArrayList<Integer>>(3);

    private volatile static SensorsDataManager sensorsDataManager = null;

    public static SensorsDataManager getInstance() {
        if (sensorsDataManager == null) {
            synchronized (SensorsDataManager.class) {
                if (sensorsDataManager == null) {
                    sensorsDataManager = new SensorsDataManager();
                }
            }
        }
        return sensorsDataManager;
    }

    public void init(MainActivity mainActivity) {
        // 下面顺便采集些磁场和方向的数据
        sensorManager = (SensorManager) mainActivity
                .getSystemService(Context.SENSOR_SERVICE);
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        osensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        asensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        grasensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        dataInit();
        mSensorListener = new MSensorListener();
        oSensorListener = new OSensorListener();
        aSensorListener = new ASensorListener();
        gSensorListener = new GSensorListener();
        graSensorListener = new GraSensorListener();
        sensorManager.registerListener(mSensorListener, msensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(oSensorListener, osensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(aSensorListener, asensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gSensorListener, gsensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(graSensorListener, grasensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void dataInit() {
        for (int i = 0; i < 3; i++) {
            dataMagnetic.add(new ArrayList<Integer>());
            dataOrientation.add(new ArrayList<Integer>());
            dataAccelerate.add(new ArrayList<Integer>());
            dataGyroscope.add(new ArrayList<Integer>());
            dataGravity.add(new ArrayList<Integer>());
        }
    }

    public void dataClear() {
        for (int i = 0; i < dataMagnetic.size(); i++) {
            dataMagnetic.get(i).clear();
            dataOrientation.get(i).clear();
            dataAccelerate.get(i).clear();
            dataGyroscope.get(i).clear();
            dataGravity.get(i).clear();
        }
    }

    public void updateSensorsData() {
        // 保持和wifi的数据采集同步，不每次都更新
        if (WifiDataManager.getInstance().dataCount > dataAccelerate.get(0)
                .size()) {
            dataMagnetic.get(0).add(
                    Integer.valueOf((int) Math.floor(temp_m[0] * 100)));
            dataMagnetic.get(1).add(
                    Integer.valueOf((int) Math.floor(temp_m[1] * 100)));
            dataMagnetic.get(2).add(
                    Integer.valueOf((int) Math.floor(temp_m[2] * 100)));
            dataOrientation.get(0).add(
                    Integer.valueOf((int) Math.floor(temp_o[0] * 100)));
            dataOrientation.get(1).add(
                    Integer.valueOf((int) Math.floor(temp_o[1] * 100)));
            dataOrientation.get(2).add(
                    Integer.valueOf((int) Math.floor(temp_o[2] * 100)));
            dataAccelerate.get(0).add(
                    Integer.valueOf((int) Math.floor(temp_a[0] * 100)));
            dataAccelerate.get(1).add(
                    Integer.valueOf((int) Math.floor(temp_a[1] * 100)));
            dataAccelerate.get(2).add(
                    Integer.valueOf((int) Math.floor(temp_a[2] * 100)));
            dataGyroscope.get(0).add(
                    Integer.valueOf((int) Math.floor(temp_g[0] * 100)));
            dataGyroscope.get(1).add(
                    Integer.valueOf((int) Math.floor(temp_g[1] * 100)));
            dataGyroscope.get(2).add(
                    Integer.valueOf((int) Math.floor(temp_g[2] * 100)));
            dataGravity.get(0).add(
                    Integer.valueOf((int) Math.floor(temp_gra[0] * 100)));
            dataGravity.get(1).add(
                    Integer.valueOf((int) Math.floor(temp_gra[1] * 100)));
            dataGravity.get(2).add(
                    Integer.valueOf((int) Math.floor(temp_gra[2] * 100)));
        }
    }

    int count = 0;
    private class MSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            temp_m[0] = event.values[0];
            temp_m[1] = event.values[1];
            temp_m[2] = event.values[2];
            // 在这里显示一下地磁数据试试


            if (count != 15)
            {
                count++;
            }
            else
            {
                MainActivity.dataTextView.setText("地磁数据: " + temp_m[0] + " "
                        + temp_m[1] + " " + temp_m[2]);
                count = 0;
            }
//            MainActivity.dataTextView.setText("地磁数据: " + temp_m[0] + " "
//                    + temp_m[1] + " " + temp_m[2]);
            updateSensorsData();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class OSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            temp_o[0] = event.values[0];
            temp_o[1] = event.values[1];
            temp_o[2] = event.values[2];
            updateSensorsData();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class ASensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            temp_a[0] = event.values[0];
            temp_a[1] = event.values[1];
            temp_a[2] = event.values[2];
            updateSensorsData();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class GSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            temp_g[0] = event.values[0];
            temp_g[1] = event.values[1];
            temp_g[2] = event.values[2];
            updateSensorsData();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class GraSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            temp_gra[0] = event.values[0];
            temp_gra[1] = event.values[1];
            temp_gra[2] = event.values[2];
            updateSensorsData();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public void unregist() {
        sensorManager.unregisterListener(mSensorListener);
        sensorManager.unregisterListener(oSensorListener);
        sensorManager.unregisterListener(aSensorListener);
        sensorManager.unregisterListener(gSensorListener);
        sensorManager.unregisterListener(graSensorListener);
    }
}





















