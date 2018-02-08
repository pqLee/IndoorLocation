package com.intel.sensorinteraction;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends Activity
        implements SensorEventListener {

    public static final String TAG = "MainActivity : ";
    private CalDistance mCalDistance;              // distance calculator
    private SensorManager mSensorManager;
    private DecimalFormat mDecimalFormat = new DecimalFormat("#.##");
    private TextView mDisTextView;
    private float[] dis = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_UI);

        mDecimalFormat.setMaximumFractionDigits(1);     // data format
        mDecimalFormat.setMinimumFractionDigits(1);

        mCalDistance = new CalDistance();

        mDisTextView = (TextView)findViewById(R.id.distanceView);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
        {
            mCalDistance.setAcceValues(event.values);
            dis = mCalDistance.acceleIntegrate1();
            mDisTextView.setText(mDecimalFormat.format(dis[0]));
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float angle = AngleChange.angleFromAccele(event.values, 0);
            Log.d("Angle : ", String.valueOf(angle));
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_UI);
    }

}

