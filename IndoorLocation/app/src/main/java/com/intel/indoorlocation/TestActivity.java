package com.intel.indoorlocation;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.intel.indoorlocation.DataManager.DBManager;
import com.intel.indoorlocation.DataManager.PointMatcher;
import com.intel.indoorlocation.DataManager.RegionMatcher;
import com.intel.indoorlocation.DataManager.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
public class TestActivity extends Activity implements SensorEventListener {
    private static String LOG_TAG = "TestActivity";
    private String tableName1;
    private String tableName2;
    private String tag;

    private Switch mSwitch;

    private float[] mAcceleData1 = new float[3];    // data collected by linear_accelerometer sensor.
    private float[] mAcceleData2 = new float[3];    // data collected by linear_accelerometer sensor.
    private float[] mVelocity1 = new float[3];
    private float[] mVelocity2 = new float[3];
    private float[] mVelocity3 = new float[3];
    float[] mDeltaAccele = new float[3];
    float[] mDeltaVelocity = new float[3];

    private DBManager mDBM;
    private SensorManager mSM;
    private Sensor mMagneticSensor;
    private Sensor mLinearAcceleSensor;
    private Sensor mAcceleSensor;

    private List<SampleData> mQueryList1 = new ArrayList<SampleData>();
    private List<SampleData> mQueryList2 = new ArrayList<SampleData>();
    private float[][] DB;
    private float[][] DB2;           // For Hausdorff location algorithm.

    float deltaTime = 0.06f;
    private int countOfLocationNum1 = 0;
    private int countOfLocationNum2 = 0;
    private int previousLocNum1 = 0;
    private int previousLocNum2 = 0;
    private PointMatcher mMatcher;
    private RegionMatcher mHauMatcher;
    private Map<Integer, String> mPos_Location;

    private float[] gravity = new float[3];
    private float ALPHA = 0.8f;

    public static double formatDouble1(double d) {
        return (double)Math.round(d*10)/10;
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Bundle bundle = this.getIntent().getExtras();
        tableName1 = bundle.getString("tableName1");
        tableName2 = bundle.getString("tableName2");

        mSwitch = (Switch)findViewById(R.id.sw_on_off);
        mSwitch.setOnCheckedChangeListener(mCheckedListener);

        mDBM = new DBManager(this);
        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagneticSensor = mSM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLinearAcceleSensor = mSM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mAcceleSensor = mSM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        queryTable1(tableName1);
        if (mQueryList1.size()!=0){
            DB = SetDBForEual(mQueryList1);
            Log.d(LOG_TAG,"DB.length is " + DB.length);
            Log.d(LOG_TAG,"DB[0][0] is " + DB[0][0]);
        }else {
            Log.d(LOG_TAG,"Table1 is null");
            Toast toast=Toast.makeText(getApplicationContext(), tableName1 + " is null", Toast.LENGTH_SHORT);
            toast.show();
        }


//        if (DB[0][0]==0){
//            Log.d(LOG_TAG,"Table1 is null");
//        }

        queryTable2(tableName2);
        if (mQueryList2.size()!=0){
            DB2 = SetDBForHausdorff(mQueryList2);
            Log.d(LOG_TAG,"DB2.length is " + DB2.length);
            Log.d(LOG_TAG,"DB2[0][0] is " + DB2[0][0]);

        }else {
            Log.d(LOG_TAG,"Table2 is null");
            Toast toast=Toast.makeText(getApplicationContext(), tableName2 + " is null", Toast.LENGTH_SHORT);
            toast.show();
        }

        mMatcher = new PointMatcher();
        mHauMatcher = new RegionMatcher();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mAcceleData2 = sensorEvent.values;

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //mRawAcceleData = sensorEvent.values;

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            //mMagneticData = sensorEvent.values;

        }
   }

    protected CompoundButton.OnCheckedChangeListener mCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Log.d(LOG_TAG, "is on");
                mSM.registerListener(TestActivity.this, mMagneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
                mSM.registerListener(TestActivity.this, mAcceleSensor, SensorManager.SENSOR_DELAY_FASTEST);
                mSM.registerListener(TestActivity.this, mLinearAcceleSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.d(LOG_TAG, "is off");
                mSM.unregisterListener(TestActivity.this);
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }



    private void queryTable1(String tableName) {
        Log.d(LOG_TAG, "queryTable");
        mQueryList1 = new ArrayList<SampleData>();
        mQueryList1 = mDBM.queryTable(tableName);
        Log.d(LOG_TAG, "Querylist size is " + mQueryList1.size());
    }
    private void queryTable2(String tableName) {
        Log.d(LOG_TAG, "queryTable");
        mQueryList2 = new ArrayList<SampleData>();
        mQueryList2 = mDBM.queryTable(tableName);
        Log.d(LOG_TAG, "Querylist size is " + mQueryList2.size());
    }

    private float[][] SetDBForEual(List<SampleData> mList) {
        float[][] database = new float[mList.size()][3];
        for (int i = 0; i < database.length; i++) {
            SampleData sd = new SampleData();
            sd = mList.get(i);
            database[i] = new float[]{sd.getmX(), sd.getmY(), sd.getmZ()};
        }
        return database;
    }

    private float[][] SetDBForHausdorff(List<SampleData> mList) {
        float[][] database = new float[mList.size()/4][12];
        for (int i = 0; i<database.length; i++){
            SampleData sd1 = new SampleData();
            SampleData sd2 = new SampleData();
            SampleData sd3 = new SampleData();
            SampleData sd4 = new SampleData();

            sd1 = mList.get(4*i);
            sd2 = mList.get(4*i+1);
            sd3 = mList.get(4*i+2);
            sd4 = mList.get(4*i+3);
            database[i] = new float[]{sd1.getmX(), sd1.getmY(), sd1.getmZ(),sd2.getmX(), sd2.getmY(), sd2.getmZ(),
                    sd3.getmX(), sd3.getmY(), sd3.getmZ(),sd4.getmX(), sd4.getmY(), sd4.getmZ(),};
        }
        return database;
    }
}

*/
