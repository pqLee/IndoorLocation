package com.intel.test.locationdemo;

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

import com.intel.test.locationdemo.data.DBManager;
import com.intel.test.locationdemo.data.SampleData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by limeng on 17-8-3.
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

        //getTabelNameList();
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

//                if (mQueryQueue.size() != 0){
//                    deleteData(tableName,locTag);
//                    //deleteSampleData(averageData);
//                }
//                queryData(locTag);
            }
        }
    };

    public void onSensorChanged(SensorEvent sensorEvent) {
        float x,y,z;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = sensorEvent.values;

            x = magneticFieldValues[0];
            y = magneticFieldValues[1];
            z = magneticFieldValues[2];

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
        mDBM.updateData(tableName,tag,mQueue);
    }

    public void queryData(String tag){
        Log.d(LOG_TAG,"query 1 datas");
        mQueryQueue = new LinkedList<SampleData>();
        mQueryQueue =  mDBM.queryTagData(tableName,tag);
        Log.d(LOG_TAG,"query size is " + mQueryQueue.size());
    }

    public void deleteData(String tableName, String tag){
        Log.d(LOG_TAG,"deleteData");
        mDBM.deleteData(tableName,tag);
    }
//    public void addSampleData(SampleData sampleData){
//        Log.d(LOG_TAG,"addSampleData");
//        mDBM.addOneData(tableName,sampleData);
//    }

//    public void updateSampleData(SampleData sampleData){
//        Log.d(LOG_TAG,"updateSampleData");
//        mDBM.updateOneData(tableName,sampleData);
//    }

//    public void deleteSampleData(SampleData sampleData){
//        Log.d(LOG_TAG,"deleteSampleData");
//        mDBM.deleteOneData(tableName,sampleData);
//    }

//    public void queryTagData(String tag){
//        Log.d(LOG_TAG,"queryTagData");
//        mQueryQueue = mDBM.queryTagData(tableName,tag);
//        if (mQueryQueue.size() == 0){
//            Log.d(LOG_TAG,"no nowData is avaliable!");
//        }
//
//    }

//    public void queryTable(){
//        Log.d(LOG_TAG,"queryTable");
//        mDBM.queryTotalData(tableName);
//    }
    public void createTable(String tableName){
        Log.d(LOG_TAG, "createTable.DB_VERSION: " + DB_VERSION);
        mDBM.createTable(tableName);
        //tableNames.add(CollectOneActivity.tableName);
    }

//    public void deleteTable(){
//        mDBM.deleteTable(tableName);
//        //tableNames.remove(tableName);
//    }

//    public void getTabelNameList(){
//        boolean flag = false;
//        List<String> list = mDBM.queryTableNameList();
//        for (String name : list) {
//            if (!name.equals("locations")) {
//                for (int i = 0; i < tableNames.size(); i ++) {
//                    if (name.equals(tableNames.get(i))) {
//                        flag = true;
//                    }
//                }
//                if (!flag) {
//                    tableNames.add(name);
//                }
//            }
//        }
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //dbManager.closeDatabase();
    }


}
