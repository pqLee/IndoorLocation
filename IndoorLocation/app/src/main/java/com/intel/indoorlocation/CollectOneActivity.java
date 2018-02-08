package com.intel.indoorlocation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.indoorlocation.DataManager.DBManager;
import com.intel.indoorlocation.DataManager.MatrixManager;
import com.intel.indoorlocation.DataManager.MovingAverage;
import com.intel.indoorlocation.DataManager.SampleData;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by limeng on 17-8-3.
 *
 */

public class CollectOneActivity extends Activity implements SensorEventListener {
    private static String LOG_TAG = "CollectOneActivity";

    private String locTag;
    private int DB_VERSION = 1;
    private Switch mSwitch;
    private TextView mTv_x;
    private TextView mTv_y;
    private TextView mTv_z;
    private EditText mEt;

    private DBManager mDBM;
    private SensorManager mSM;
    private Sensor magnetic;

    //private static List<String> tableNames = new ArrayList<String>();
    private float[] magneticFieldValues = new float[3];

    private Queue<SampleData> mCalculateQueue = new LinkedList<SampleData>();
    private Queue<SampleData> mQueryQueue = new LinkedList<SampleData>();
    private Queue<SampleData> mQueue = new LinkedList<SampleData>();

    private SampleData nowData = new SampleData();
    private SampleData latestData = new SampleData();
    private SampleData averageData = new SampleData();
    public  String tableName = "locations";

    private float x_average = 0;
    private float y_average = 0;
    private float z_average = 0;

    private  MovingAverage movingAverage0 = new MovingAverage(40);
    private  MovingAverage movingAverage1 = new MovingAverage(40);
    private  MovingAverage movingAverage2 = new MovingAverage(40);
    private  float[] rotate = new float[16];
    private  float[] remapR = new float[16];
    private  float[] angleInRad = new float[3];

    private float[] mAcceleData = new float[3];
    private float[] magRot = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        Bundle bundle = this.getIntent().getExtras();
        tableName = bundle.getString("tableName");

        initComponent();

        mDBM = new DBManager(this);
        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetic = mSM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        createTable(tableName);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    private void initComponent() {
        mSwitch = (Switch)findViewById(R.id.sw_on_off);
        mSwitch.setOnCheckedChangeListener(mCheckedListener);
        mTv_x = (TextView)findViewById(R.id.tv_x);
        mTv_y = (TextView)findViewById(R.id.tv_y);
        mTv_z = (TextView)findViewById(R.id.tv_z);
        mEt = (EditText)findViewById(R.id.et_Location);
    }

    protected CompoundButton.OnCheckedChangeListener mCheckedListener = new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if (isChecked) {
                Log.d(LOG_TAG,"is on");
                mSM.registerListener(CollectOneActivity.this,
                        magnetic, SensorManager.SENSOR_DELAY_UI);

                if (TextUtils.isEmpty(mEt.getText())){
                    Log.d(LOG_TAG, "Please input location");
                    Toast toast=Toast.makeText(getApplicationContext(), "Please input location", Toast.LENGTH_SHORT);
                    toast.show();
                    locTag = "DEFAULT";
                }
                else {
                    Log.d(LOG_TAG,"Your Tag is " + mEt.getText());
                    locTag = mEt.getText().toString();
                    Log.d(LOG_TAG,"Your locTag is " + locTag);
                }
            } else {
                Log.d(LOG_TAG,"is off");
                String tag;
                float x_total = 0;
                float y_total = 0;
                float z_total = 0;
                mSM.unregisterListener(CollectOneActivity.this);
                int size = mCalculateQueue.size();
                for (int i =0;i<size;i++){
                    latestData = mCalculateQueue.remove();
                    Log.d(LOG_TAG,"Now count is: "+ String.valueOf(i));
                    Log.d(LOG_TAG,"Now X is: "+ latestData.mX);
                    Log.d(LOG_TAG,"Now Y is: "+ latestData.mY);
                    Log.d(LOG_TAG,"Now Z is: "+ latestData.mZ);
                    Log.d(LOG_TAG,"Now TAG is: "+ latestData.mTag);
                    x_total += latestData.mX;
                    y_total += latestData.mY;
                    z_total += latestData.mZ;
                    latestData = new SampleData();
                }
                mCalculateQueue.clear();

                calculateAverage(x_total,y_total,z_total,size);
                tag = averageData.getmTag();
                queryData(tag);

                if (!tag.equals("DEFAULT")){
                    mQueue = new LinkedList<SampleData>();
                    mQueue.add(averageData);

                    if (mQueryQueue.size() == 0){
                        addData(tableName,mQueue);
                        //addSampleData(averageData);
                    }else {
                        updateData(tableName,tag,mQueue);
                        //updateSampleData(averageData);
                    }
                    queryData(tag);
                }
            }
        }
    };

    public void onSensorChanged(SensorEvent sensorEvent) {
        float x,y,z;

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mAcceleData = sensorEvent.values;

        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = sensorEvent.values;

            MagneticDataProcess();

            movingAverage0.pushValue(magneticFieldValues[0]);       // Filter
            magneticFieldValues[0] = movingAverage0.getValue();
            movingAverage1.pushValue(magneticFieldValues[1]);
            magneticFieldValues[1] = movingAverage1.getValue();
            movingAverage2.pushValue(magneticFieldValues[2]);
            magneticFieldValues[2] = movingAverage2.getValue();

            x = magneticFieldValues[0];       // Raw data
            y = magneticFieldValues[1];
            z = magneticFieldValues[2];
            //x = magRot[0];                  // Data after Coordinate Conversion
            //y = magRot[1];
            //z = magRot[2];

            mTv_x.setText(String.valueOf(x));
            mTv_y.setText(String.valueOf(y));
            mTv_z.setText(String.valueOf(z));

            nowData = new SampleData();
            nowData.setmX(x);
            nowData.setmY(y);
            nowData.setmZ(z);
            nowData.setmTag(locTag);

            if (mCalculateQueue.size()>=10){
                mCalculateQueue.poll();
            }
            mCalculateQueue.offer(nowData);
            Log.d(LOG_TAG,"dataX is " + String.valueOf(nowData.mX));
            Log.d(LOG_TAG,"dataY is " + String.valueOf(nowData.mY));
            Log.d(LOG_TAG,"dataZ is " + String.valueOf(nowData.mZ));
            Log.d(LOG_TAG,"dataTag is " + nowData.mTag);
        }

    }

    public void calculateAverage(float x_total, float y_total, float z_total, int size){
        x_average = x_total/size;
        y_average = y_total/size;
        z_average = z_total/size;

        averageData = new SampleData();
        averageData.setmX(x_average);
        averageData.setmY(y_average);
        averageData.setmZ(z_average);
        averageData.setmTag(locTag);

        Log.d(LOG_TAG, "x_average is " + x_average);
        Log.d(LOG_TAG, "y_average is " + y_average);
        Log.d(LOG_TAG, "z_average is " + z_average);
    }

    public void addData(String tableName, Queue<SampleData> mQueue){
        Log.d(LOG_TAG,"addData 1 datas");
        mDBM.add(tableName,mQueue);
    }
    public void updateData(String tableName, String tag, Queue<SampleData> mQueue){
        Log.d(LOG_TAG,"updateData");
        mDBM.updateData(tableName, tag, mQueue);
    }

    public void queryData(String tag){
        Log.d(LOG_TAG,"query 1 datas");
        mQueryQueue = new LinkedList<SampleData>();
        mQueryQueue =  mDBM.queryTagData(tableName,tag);
        Log.d(LOG_TAG, "query size is " + mQueryQueue.size());
    }

    public void deleteData(String tableName, String tag){
        Log.d(LOG_TAG,"deleteData");
        mDBM.deleteData(tableName, tag);
    }

    public void createTable(String tableName){
        Log.d(LOG_TAG, "createTable.DB_VERSION: " + DB_VERSION);
        mDBM.createTable(tableName);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void MagneticDataProcess()
    {
        movingAverage0.pushValue(magneticFieldValues[0]);
        magneticFieldValues[0] = movingAverage0.getValue();
        movingAverage1.pushValue(magneticFieldValues[1]);
        magneticFieldValues[1] = movingAverage1.getValue();
        movingAverage2.pushValue(magneticFieldValues[2]);
        magneticFieldValues[2] = movingAverage2.getValue();

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
        float[] magIni = new float[]{magneticFieldValues[0], magneticFieldValues[1], magneticFieldValues[2]};
        magRot = MatrixManager.getMultiVector(Rzyx, magIni);
    }

    private void GetOrientation()
    {
        SensorManager.getRotationMatrix(rotate, null, mAcceleData, magneticFieldValues);
        SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remapR);
        SensorManager.getOrientation(remapR, angleInRad);
    }
}
