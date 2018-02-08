package com.intel.indoorlocation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.indoorlocation.DataManager.DBManager;
import com.intel.indoorlocation.DataManager.PointMatcher;
import com.intel.indoorlocation.DataManager.RegionMatcher;
import com.intel.indoorlocation.DataManager.MatrixManager;
import com.intel.indoorlocation.DataManager.MovingAverage;
import com.intel.indoorlocation.DataManager.SampleData;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends Activity {
    private static final String LOG_TAG = "LocationActivity";
    private List<SampleData> mQueryList1 = new ArrayList<>();
    private List<SampleData> mQueryList2 = new ArrayList<>();
    private DBManager mDBM;

    private TextView mMagneticView;

    private TextView locshowView;
    private SensorManager mSensorManager;
    private MySensorListener mSensorListener;

    private float[] mAcceleData1 = new float[3];    // data collected by linear_accelerometer sensor.
    private float[] mAcceleData2 = new float[3];    // data collected by linear_accelerometer sensor.

    private float[] mRawAcceleData = new float[3]; // data collected by accelerometer sensor.
    private float[] mMagneticData = new float[3];  // data collected by magnetic_field sensor.

    private MovingAverage movingAverage3;
    private MovingAverage movingAverage4;
    private MovingAverage movingAverage5;
    private MovingAverage movingAverage6;   // For accelerometer sensor data
    private MovingAverage movingAverage7;
    private MovingAverage movingAverage8;
    private float[] rotate = new float[16];
    private float[] remapR = new float[16];
    private float[] angleInRad = new float[3];
    private float[] magRot = new float[3];

    private int countOfLocationNum1 = 0;
    private int previousLocNum1 = 0;
    private int previousLocNum2 = 0;

    private float[][] DB;
    private float[][] DB2;                                // For Hausdorff location algorithm.

    private PointMatcher mMatcher;
    private RegionMatcher mHauMatcher;

    private float[] gravity = new float[3];
    private float ALPHA = 0.8f;

    public static double formatDouble1(double d) {
        return (double) Math.round(d * 10) / 10;
    }

    private TextView mPositionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Log.e(LOG_TAG, "come in location activity");

        Bundle bundle = this.getIntent().getExtras();
        String tableName1 = bundle.getString("tableName1");
        String tableName2 = bundle.getString("tableName2");

        mPositionView = (TextView)findViewById(R.id.position);
        locshowView = (TextView)findViewById(R.id.loc_xyz);

        mSensorListener = new MySensorListener();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mLinearAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor mAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(mSensorListener, mLinearAcceleSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mAcceleSensor, SensorManager.SENSOR_DELAY_UI);

        mDBM = new DBManager(this);
        queryTable1(tableName1);
        Log.d(LOG_TAG, "mQueryList1 size" + mQueryList1.size());
        if (mQueryList1.size()!=0){
            DB = SetDBForEual(mQueryList1);
            Log.d(LOG_TAG,"DB[].length is " + DB[0].length);
            Log.d(LOG_TAG,"DB[0][0] is " + DB[0][0]);
        }else {
            Log.d(LOG_TAG,"Table1 is null");
            Toast toast=Toast.makeText(getApplicationContext(), tableName1 + " is null", Toast.LENGTH_SHORT);
            toast.show();
        }

        queryTable2(tableName2);
        Log.d(LOG_TAG, "mQueryList2 size" + mQueryList2.size());
        if (mQueryList2.size()!=0){
            DB2 = SetDBForHausdorff(mQueryList2);
            Log.d(LOG_TAG,"DB2[].length is " + DB2[0].length);      //
            Log.d(LOG_TAG,"DB2[0][0] is " + DB2[0][0]);

        }else {
            Log.d(LOG_TAG,"Table2 is null");
            Toast toast=Toast.makeText(getApplicationContext(), tableName2 + " is null", Toast.LENGTH_SHORT);
            toast.show();
        }

        Log.e("database---1 : ", "x : " + DB[0][0] + " y : " + DB[0][1] + " z : " + DB[0][2]);
        Log.e("database---2 : ", "x : " + DB2[0][0] + " y : " + DB2[0][1] + " z : " + DB2[0][2]);
        Log.e("database---2 : ", "x : " + DB2[0][3] + " y : " + DB2[0][4] + " z : " + DB2[0][5]);
        Log.e("database---2 : ", "x : " + DB2[0][6] + " y : " + DB2[0][7] + " z : " + DB2[0][8]);
        Log.e("database---2 : ", "x : " + DB2[0][9] + " y : " + DB2[0][10] + " z : " + DB2[0][11]);

        movingAverage3 = new MovingAverage(40);
        movingAverage4 = new MovingAverage(40);
        movingAverage5 = new MovingAverage(40);
        movingAverage6 = new MovingAverage(40);
        movingAverage7 = new MovingAverage(40);
        movingAverage8 = new MovingAverage(40);

        mMatcher = new PointMatcher();
        mHauMatcher = new RegionMatcher();
    }

    private class MySensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                mAcceleData2[0] = event.values[0];
                mAcceleData2[1] = event.values[1];
                mAcceleData2[2] = event.values[2];
                //LinearVelocityCal();
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mRawAcceleData = event.values;
                AcceleDataProcess();
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mMagneticData[0] = event.values[0];
                mMagneticData[1] = event.values[1];
                mMagneticData[2] = event.values[2];

                movingAverage3.pushValue(mMagneticData[0]);
                mMagneticData[0] = movingAverage3.getValue();
                movingAverage4.pushValue(mMagneticData[1]);
                mMagneticData[1] = movingAverage4.getValue();
                movingAverage5.pushValue(mMagneticData[2]);
                mMagneticData[2] = movingAverage5.getValue();

                // add a Filter
/*                float[] filteredValue = new float[3];
                filteredValue[0] = mMagneticData[0] * ALPHA + filteredValue[0] * (1.0f - ALPHA);
                filteredValue[1] = mMagneticData[1] * ALPHA + filteredValue[1] * (1.0f - ALPHA);
                filteredValue[2] = mMagneticData[2] * ALPHA + filteredValue[2] * (1.0f - ALPHA);
                mMagneticData = filteredValue;*/

                MagneticDataProcess();

                locshowView.setText("x:"+String.valueOf(mMagneticData[0])+"\n"+
                        "y:"+String.valueOf(mMagneticData[1])+"\n"+
                        "z:"+String.valueOf(mMagneticData[2])+"\n");
            }

            //Log.e("sampled data : ", "x : " + mMagneticData[0] + " y : " + mMagneticData[1] + " z : " + mMagneticData[2]);
            //Log.e("processed data : ", "x : " + magRot[0] + " y : " + magRot[1] + " z : " + magRot[2]);

            int Location_Number = 0;
            /**
             * 点精确匹配,基于Eucl距离
             * **/

            /**
             * 区域模糊匹配,基于Hausdorff距离,
             */
            Log.d("Dividing line : ", "****************");
            /************ 综合上述两种判断!!! ************/
            int Location_Number_Eucl = mMatcher.Matcher(DB, mMagneticData);          // Raw data compare
            Log.d("Location_Number_Eucl : ", String.valueOf(Location_Number_Eucl));

            int Location_Number_Haus = mHauMatcher.Matcher(DB2, mMagneticData);
            Log.d("Location_Number_Haus : ", String.valueOf(Location_Number_Haus));

            // -----多次出现同一个位置才会认为是某个位置（减少跳动的可能）
            if (Location_Number_Eucl == Location_Number_Haus)
            {
                countOfLocationNum1++;
                previousLocNum1 = Location_Number_Eucl;
            }
            else
            {
                countOfLocationNum1 = 0;
                previousLocNum1 = Location_Number_Haus;
            }

            if (countOfLocationNum1 > 50)                                 // Set the stable value
            {
                //Log.d("**Current Position** : ", String.valueOf(Location_Number_Eucl));
                //mPositionView.setText(mPos_Location.get(Location_Number_Eucl));
                mPositionView.setText(String.valueOf(Location_Number_Eucl));
            }
            else
            {
                //Log.e("Not found position : ", "None");
                mPositionView.setText(String.valueOf("not match"));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }

    private void MagneticDataProcess() {
        movingAverage3.pushValue(mMagneticData[0]);
        mMagneticData[0] = movingAverage3.getValue();
        movingAverage4.pushValue(mMagneticData[1]);
        mMagneticData[1] = movingAverage4.getValue();
        movingAverage5.pushValue(mMagneticData[2]);
        mMagneticData[2] = movingAverage5.getValue();

        GetOrientation();
        // 3*3 matrix
        float[][] Rz = new float[][]{
                {(float) Math.cos(angleInRad[0]), (float) -Math.sin(angleInRad[0]), 0},
                {(float) Math.sin(angleInRad[0]), (float) Math.cos(angleInRad[0]), 0},
                {0, 0, 1}};
        float[][] Rx = new float[][]{
                {1, 0, 0},
                {0, (float) Math.cos(angleInRad[1]), (float) -Math.sin(angleInRad[1])},
                {0, (float) Math.sin(angleInRad[1]), (float) Math.cos(angleInRad[1])}};
        float[][] Ry = new float[][]{
                {(float) Math.cos(angleInRad[2]), 0, (float) -Math.sin(angleInRad[2])},
                {0, 1, 0},
                {(float) Math.sin(angleInRad[2]), 0, (float) Math.cos(angleInRad[2])}};
        // Rz*Ry*Rx
        float[][] Rzy = MatrixManager.getMultiply(Rz, Ry);
        float[][] Rzyx = MatrixManager.getMultiply(Rzy, Rx);
        // 求旋转矩阵的逆矩阵
        Rzyx = MatrixManager.getNi3(Rzyx);

        float[] magIni = new float[]{mMagneticData[0], mMagneticData[1], mMagneticData[2]};
        magRot = MatrixManager.getMultiVector(Rzyx, magIni);
    }

    private void AcceleDataProcess() {
        // Simple Moving Average.
        movingAverage6.pushValue(mRawAcceleData[0]);
        mRawAcceleData[0] = movingAverage6.getValue();
        movingAverage7.pushValue(mRawAcceleData[1]);
        mRawAcceleData[1] = movingAverage7.getValue();
        movingAverage8.pushValue(mRawAcceleData[2]);
        mRawAcceleData[2] = movingAverage8.getValue();
    }

    private void GetOrientation() {
        SensorManager.getRotationMatrix(rotate, null, mRawAcceleData, mMagneticData);
        SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remapR);
        SensorManager.getOrientation(remapR, angleInRad);
    }

    private void queryTable1(String tableName) {
        Log.d(LOG_TAG, "queryTable");
        mQueryList1 = new ArrayList<>();
        mQueryList1 = mDBM.queryTable(tableName);
        Log.d(LOG_TAG, "Querylist size is " + mQueryList1.size());
    }
    private void queryTable2(String tableName) {
        Log.d(LOG_TAG, "queryTable");
        mQueryList2 = new ArrayList<>();
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





/*
public class LocationActivity extends Activity{
    private static final String LOG_TAG = "LocationActivity";

    private TextView mPosTextView;

    private DBManager mDBM;
    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mLinearAcceleSensor;
    private Sensor mAcceleSensor;

    private List<SampleData> mQueryList1 = new ArrayList<>();
    private List<SampleData> mQueryList2 = new ArrayList<>();
    private float[][] DB;
    private float[][] DB2;           // For Hausdorff location algorithm.

    private PointMatcher mMatcher;
    private RegionMatcher mHauMatcher;
    private Map<Integer, String> mPos_Location;

    private float[] mAcceleData = new float[3];
    private float[] mMagneticData = new float[3];
    private float[] rotate = new float[16];
    private float[] remapR = new float[16];
    private float[] angleInRad = new float[3];
    private float[] magRot = new float[3];
    private int countOfLocationNum1 = 0;

    private MovingAverage movingAverage0;
    private MovingAverage movingAverage1;
    private MovingAverage movingAverage2;
    private MovingAverage movingAverage3;
    private MovingAverage movingAverage4;
    private MovingAverage movingAverage5;
    private MySensorListener mySensorListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Bundle bundle = this.getIntent().getExtras();
        String tableName1 = bundle.getString("tableName1");
        String tableName2 = bundle.getString("tableName2");

        mPosTextView = (TextView)findViewById(R.id.position);  // Show the current position message

        Switch mSwitch = (Switch) findViewById(R.id.sw_on_off);
        mSwitch.setOnCheckedChangeListener(mCheckedListener);

        mySensorListener = new MySensorListener();
        mDBM = new DBManager(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLinearAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
        //String str = mQueryList1.get(0).getmTag();

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

        movingAverage0 = new MovingAverage(5);
        movingAverage1 = new MovingAverage(5);
        movingAverage2 = new MovingAverage(5);
        movingAverage3 = new MovingAverage(40);
        movingAverage4 = new MovingAverage(40);
        movingAverage5 = new MovingAverage(40);

        mMatcher = new PointMatcher();
        mHauMatcher = new RegionMatcher();
    }

    private class MySensorListener implements SensorEventListener
    {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                mAcceleData[0] = event.values[0];
                mAcceleData[1] = event.values[1];
                mAcceleData[2] = event.values[2];
                AcceleDataProcess();
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mMagneticData[0] = event.values[0];
                mMagneticData[1] = event.values[1];
                mMagneticData[2] = event.values[2];

                // add a Filter
                float[] filteredValue = new float[3];
                float ALPHA = 0.8f;
                filteredValue[0] = mMagneticData[0] * ALPHA + filteredValue[0] * (1.0f - ALPHA);
                filteredValue[1] = mMagneticData[1] * ALPHA + filteredValue[1] * (1.0f - ALPHA);
                filteredValue[2] = mMagneticData[2] * ALPHA + filteredValue[2] * (1.0f - ALPHA);
                //Log.d("filteredValue[0] : ", String.valueOf(filteredValue[0]));
                //mMagneticData = filteredValue;

                MagneticDataProcess();
            }

            Log.d("****" + "raw data : ", String.valueOf(mMagneticData[0]));
            Log.d("****" + "rot data : ", String.valueOf(magRot[0]));



            int Location_Number_Eucl = mMatcher.PointMatcher(DB, magRot);
            int Location_Number_Haus = mHauMatcher.RegionMatcher(DB2, magRot);

            //Log.d("DataBase : ", String.valueOf(DB[0][0]) + String.valueOf(DB[0][1]) + String.valueOf(DB[0][2]));
            //Log.d("Sample Data : ",  String.valueOf(magRot[0]) + String.valueOf(magRot[1]) + String.valueOf(magRot[2]));

            //Log.d("***Pos1 : ", String.valueOf(Location_Number_Eucl));
            //Log.d("@@@Pos2 : ", String.valueOf(Location_Number_Haus));
            if (Location_Number_Eucl == Location_Number_Haus)
                countOfLocationNum1++;
            else
                countOfLocationNum1 = 0;

            if (countOfLocationNum1 > 50)                                 // Set the stable value
            {
                // Get data object
                SampleData data1 = mQueryList1.get(Location_Number_Eucl);
                String tag1 = data1.getmTag();
                mPosTextView.setText(tag1);
                Log.d("**Current Position** : ", String.valueOf(Location_Number_Eucl));
            }
            else
            {
                mPosTextView.setText("Positioning...");
                Log.e("Not found position : ", "None");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    protected CompoundButton.OnCheckedChangeListener mCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Log.d(LOG_TAG, "is on");
                mSensorManager.registerListener(mySensorListener, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(mySensorListener, mAcceleSensor, SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(mySensorListener, mLinearAcceleSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Log.d(LOG_TAG, "is off");
                mSensorManager.unregisterListener(mySensorListener);
            }
        }
    };

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

    private void MagneticDataProcess()
    {
        movingAverage0.pushValue(mMagneticData[0]);
        mMagneticData[0] = movingAverage0.getValue();
        movingAverage1.pushValue(mMagneticData[1]);
        mMagneticData[1] = movingAverage1.getValue();
        movingAverage2.pushValue(mMagneticData[2]);
        mMagneticData[2] = movingAverage2.getValue();

        GetOrientation();
        // 3*3 matrix
        float[][] Rz = new float[][]{
                {(float) Math.cos(angleInRad[0]), (float) -Math.sin(angleInRad[0]),0},
                {(float) Math.sin(angleInRad[0]), (float) Math.cos(angleInRad[0]), 0},
                { 0,                   0,                                          1}   };
        float[][] Rx = new float[][]{
                {1,                             0,                                  0},
                {0, (float) Math.cos(angleInRad[1]), (float) -Math.sin(angleInRad[1])},
                {0, (float) Math.sin(angleInRad[1]), (float) Math.cos(angleInRad[1]) }  };
        float[][] Ry = new float[][]{
                {(float) Math.cos(angleInRad[2]), 0, (float) -Math.sin(angleInRad[2])},
                {0,                           1,                            0},
                {(float) Math.sin(angleInRad[2]), 0, (float) Math.cos(angleInRad[2])}   };
        // Rz*Ry*Rx
        float[][] Rzy = MatrixManager.getMultiply(Rz, Ry);
        float[][] Rzyx = MatrixManager.getMultiply(Rzy, Rx);
        // 求旋转矩阵的逆矩阵
        Rzyx = MatrixManager.getNi3(Rzyx);
        // device测量的磁场强度
        float[] magIni = new float[]{mMagneticData[0], mMagneticData[1], mMagneticData[2]};
        magRot = MatrixManager.getMultiVector(Rzyx, magIni);
    }

    private void GetOrientation()
    {
        SensorManager.getRotationMatrix(rotate, null, mAcceleData, mMagneticData);
        SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remapR);
        SensorManager.getOrientation(remapR, angleInRad);
    }

    private void AcceleDataProcess ()
    {
        // Simple Moving Average.
        movingAverage3.pushValue(mAcceleData[0]);
        mAcceleData[0] = movingAverage3.getValue();
        movingAverage4.pushValue(mAcceleData[1]);
        mAcceleData[1] = movingAverage4.getValue();
        movingAverage5.pushValue(mAcceleData[2]);
        mAcceleData[2] = movingAverage5.getValue();
    }


    private void queryTable1(String tableName) {
        Log.d(LOG_TAG, "queryTable");
        mQueryList1 = new ArrayList<>();
        mQueryList1 = mDBM.queryTable(tableName);
        Log.d(LOG_TAG, "Querylist size is " + mQueryList1.size());
    }
    private void queryTable2(String tableName) {
        Log.d(LOG_TAG, "queryTable");
        mQueryList2 = new ArrayList<>();
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

























/**
 * int count = 0;
 int count1 = 0;
 private void MagneticDataProcess()
 {
 movingAverage3.pushValue(mMagneticData[0]);
 mMagneticData[0] = movingAverage3.getValue();
 movingAverage4.pushValue(mMagneticData[1]);
 mMagneticData[1] = movingAverage4.getValue();
 movingAverage5.pushValue(mMagneticData[2]);
 mMagneticData[2] = movingAverage5.getValue();

 GetOrientation();
 // 3*3 matrix
 float[][] Rz = new float[][]{
 {(float) Math.cos(angleInRad[0]), (float) -Math.sin(angleInRad[0]),0},
 {(float) Math.sin(angleInRad[0]), (float) Math.cos(angleInRad[0]), 0},
 { 0,                   0,                                          1}   };
 float[][] Rx = new float[][]{
 {1,                             0,                                  0},
 {0, (float) Math.cos(angleInRad[1]), (float) -Math.sin(angleInRad[1])},
 {0, (float) Math.sin(angleInRad[1]), (float) Math.cos(angleInRad[1]) }  };
 float[][] Ry = new float[][]{
 {(float) Math.cos(angleInRad[2]), 0, (float) -Math.sin(angleInRad[2])},
 {0,                           1,                            0},
 {(float) Math.sin(angleInRad[2]), 0, (float) Math.cos(angleInRad[2])}   };
 // Rz*Ry*Rx
 float[][] Rzy = MatrixManager.getMultiply(Rz, Ry);
 float[][] Rzyx = MatrixManager.getMultiply(Rzy, Rx);
 // 求旋转矩阵的逆矩阵
 Rzyx = MatrixManager.getNi3(Rzyx);
 // 设备测量的磁场强度
 float[] magIni = new float[]{mMagneticData[0], mMagneticData[1], mMagneticData[2]};
 magRot = MatrixManager.getMultiVector(Rzyx, magIni);
 if (count1 != 20)
 {
 count1++;
 }
 else
 {
 //            mMagneticView.setText("Magnetic_Pro: " + formatDouble1(magRot[0]) + ",    " +
 //                    formatDouble1(magRot[1]) + ",    " + formatDouble1(magRot[2]));
 count1 = 0;
 }

 if (count != 15)    // 传感器刷新20次(1.2s)，更新一次数据.
 {
 count++;
 }
 else
 {
 mOlderMagData = mNewestMagData;
 mNewestMagData = magRot;
 count = 0;
 }
 }

 private void AcceleDataProcess ()
 {
 // Simple Moving Average.
 movingAverage6.pushValue(mRawAcceleData[0]);
 mRawAcceleData[0] = movingAverage6.getValue();
 movingAverage7.pushValue(mRawAcceleData[1]);
 mRawAcceleData[1] = movingAverage7.getValue();
 movingAverage8.pushValue(mRawAcceleData[2]);
 mRawAcceleData[2] = movingAverage8.getValue();
 }

 private void GetOrientation()
 {
 SensorManager.getRotationMatrix(rotate, null, mRawAcceleData, mMagneticData);
 SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remapR);
 SensorManager.getOrientation(remapR, angleInRad);
 }

 */








