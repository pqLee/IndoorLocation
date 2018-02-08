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
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.indoorlocation.DataManager.DBManager;
import com.intel.indoorlocation.DataManager.MatrixManager;
import com.intel.indoorlocation.DataManager.MovingAverage;
import com.intel.indoorlocation.DataManager.SampleData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by qixing on 17-8-10.
 *
 */

public class CollectFourActivity extends Activity implements SensorEventListener {
    private static String LOG_TAG = "CollectFourActivity";
    private static int DATA_COUNT = 0x04;
    private static int NULLDATA_COUNT = 0x00;
    private static int CALCULATE_COUNT = 0x0a;
    private String locTag = "DEFAULT";
    private String directionTag;


    private int DB_VERSION = 1;
    public String tableName;
    private Spinner mSpinner;
    private Switch mSwitch;
    private TextView mTv_x;
    private TextView mTv_y;
    private TextView mTv_z;
    private EditText mEt;

    private DBManager mDBM;
    private SensorManager mSM;
    private Sensor magnetic;

    private SampleData sd_northwest = new SampleData();
    private SampleData sd_northeast = new SampleData();
    private SampleData sd_southwest = new SampleData();
    private SampleData sd_southeast = new SampleData();

    private SampleData nowData = new SampleData();
    private SampleData latestData = new SampleData();

    private float x_average = 0;
    private float y_average = 0;
    private float z_average = 0;

    private float[] magneticFieldValues = new float[3];

    private Queue<SampleData> mCalculateQueue = new LinkedList<SampleData>();
    private Queue<SampleData> mQueryQueue = new LinkedList<SampleData>();
    private Queue<SampleData> mQueue = new LinkedList<SampleData>();

    private String ids[];

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
        setContentView(R.layout.activity_collect4);
        Bundle bundle = this.getIntent().getExtras();
        tableName = bundle.getString("tableName");

        initComponent();

        mDBM = new DBManager(this);
        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetic = mSM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        createTable(tableName);
    }

    private void initComponent() {
        mSwitch = (Switch)findViewById(R.id.sw_on_off);
        mSwitch.setOnCheckedChangeListener(mCheckedListener);
        mTv_x = (TextView)findViewById(R.id.tv_x);
        mTv_y = (TextView)findViewById(R.id.tv_y);
        mTv_z = (TextView)findViewById(R.id.tv_z);
        mEt = (EditText)findViewById(R.id.et_Location);
        mSpinner = (Spinner)findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
    }

    protected class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String[] directions = getResources().getStringArray(R.array.DIRECTIONS);
            directionTag = directions[pos];
            Log.d(LOG_TAG,"directionTag is " + directionTag);
            //Toast.makeText(CollectFourActivity.this, "your click is " + directionTag, Toast.LENGTH_SHORT).show();
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    protected CompoundButton.OnCheckedChangeListener mCheckedListener = new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if (isChecked) {
                Log.d(LOG_TAG,"is on");
                mSM.registerListener(CollectFourActivity.this,
                        magnetic, SensorManager.SENSOR_DELAY_UI);

                String nowTag = locTag;
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
                if (!nowTag.equals(locTag)){
                    sd_northwest = new SampleData();
                    sd_northeast = new SampleData();
                    sd_southwest = new SampleData();
                    sd_southeast = new SampleData();
                }
            } else {
                Log.d(LOG_TAG,"is off");
                mSM.unregisterListener(CollectFourActivity.this);
                float x_total = 0;
                float y_total = 0;
                float z_total = 0;
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
                if (directionTag.equals("NorthWest")){
                    sd_northwest = new SampleData();
                    sd_northwest.setmX(x_average);
                    sd_northwest.setmY(y_average);
                    sd_northwest.setmZ(z_average);
                    sd_northwest.setmTag(locTag);
                }
                if (directionTag.equals("NorthEast")){
                    sd_northeast = new SampleData();
                    sd_northeast.setmX(x_average);
                    sd_northeast.setmY(y_average);
                    sd_northeast.setmZ(z_average);
                    sd_northeast.setmTag(locTag);
                }
                if (directionTag.equals("SouthWest")){
                    sd_southwest = new SampleData();
                    sd_southwest.setmX(x_average);
                    sd_southwest.setmY(y_average);
                    sd_southwest.setmZ(z_average);
                    sd_southwest.setmTag(locTag);
                }
                if (directionTag.equals("SouthEast")){
                    sd_southeast = new SampleData();
                    sd_southeast.setmX(x_average);
                    sd_southeast.setmY(y_average);
                    sd_southeast.setmZ(z_average);
                    sd_southeast.setmTag(locTag);
                }
                if ((sd_northwest.getmX()!= 0)&&
                        (sd_northeast.getmX()!= 0) &&
                        (sd_southwest.getmX()!= 0) &&
                        (sd_southeast.getmX()!= 0))
                {
                    String tag;
                    mQueue = new LinkedList<SampleData>();
                    Log.d(LOG_TAG,"sd_northwest locTag is " + sd_northwest.getmTag());
                    Log.d(LOG_TAG,"sd_northwest x is " + sd_northwest.getmX());
                    Log.d(LOG_TAG,"sd_northwest y is " + sd_northwest.getmY());
                    Log.d(LOG_TAG,"sd_northwest z is " + sd_northwest.getmZ());

                    Log.d(LOG_TAG,"sd_northeast locTag is " + sd_northeast.getmTag());
                    Log.d(LOG_TAG,"sd_northeast x is " + sd_northeast.getmX());
                    Log.d(LOG_TAG,"sd_northeast y is " + sd_northeast.getmY());
                    Log.d(LOG_TAG,"sd_northeast z is " + sd_northeast.getmZ());

                    Log.d(LOG_TAG,"sd_southwest locTag is " + sd_southwest.getmTag());
                    Log.d(LOG_TAG,"sd_southwest x is " + sd_southwest.getmX());
                    Log.d(LOG_TAG,"sd_southwest y is " + sd_southwest.getmY());
                    Log.d(LOG_TAG,"sd_southwest z is " + sd_southwest.getmZ());

                    Log.d(LOG_TAG,"sd_southeast locTag is " + sd_southeast.getmTag());
                    Log.d(LOG_TAG,"sd_southeast x is " + sd_southeast.getmX());
                    Log.d(LOG_TAG,"sd_southeast y is " + sd_southeast.getmY());
                    Log.d(LOG_TAG,"sd_southeast z is " + sd_southeast.getmZ());

                    Log.d(LOG_TAG,"4 datas are prepared");

                    tag = sd_northeast.getmTag();
                    if (!tag.equals("DEFAULT")){
                        mQueue.add(sd_northwest);
                        mQueue.add(sd_northeast);
                        mQueue.add(sd_southwest);
                        mQueue.add(sd_southeast);
                        Log.d(LOG_TAG,"mQueue.size is:" + mQueue.size());
                        queryData(tag);
                        if (mQueryQueue.size()==NULLDATA_COUNT){
                            addData(tableName, mQueue);
                        }
                        else if ((mQueryQueue.size()!=NULLDATA_COUNT) && (mQueryQueue.size()!=DATA_COUNT)){
                            deleteData(tableName,tag);
                            addData(tableName, mQueue);
                        }else {
                            updateData(tableName,tag,mQueue);
                        }
                        sd_northwest = new SampleData();
                        sd_northeast = new SampleData();
                        sd_southwest = new SampleData();
                        sd_southeast = new SampleData();
                        queryData(locTag);
                    }else {
                        sd_northwest = new SampleData();
                        sd_northeast = new SampleData();
                        sd_southwest = new SampleData();
                        sd_southeast = new SampleData();
                    }

                    //deleteData(tableName,locTag);
                }else {
                    Log.e(LOG_TAG,"lost data");
                }

            }
        }
    };

    public void onSensorChanged(SensorEvent sensorEvent) {
        float x,y,z;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = sensorEvent.values;

            // Update the processed data to the database.
            MagneticDataProcess();

            //x = magRot[0];
            //y = magRot[1];
            //z = magRot[2];

            movingAverage0.pushValue(magneticFieldValues[0]);
            magneticFieldValues[0] = movingAverage0.getValue();
            movingAverage1.pushValue(magneticFieldValues[1]);
            magneticFieldValues[1] = movingAverage1.getValue();
            movingAverage2.pushValue(magneticFieldValues[2]);
            magneticFieldValues[2] = movingAverage2.getValue();

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

            if (mCalculateQueue.size()>=CALCULATE_COUNT){
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
        Log.d(LOG_TAG, "x_average is " + x_average);
        Log.d(LOG_TAG, "y_average is " + y_average);
        Log.d(LOG_TAG, "z_average is " + z_average);
    }


    public void createTable(String tableName){
        Log.d(LOG_TAG, "createTable.DB_VERSION: " + DB_VERSION);
        mDBM.createTable(tableName);
        //tableNames.add(CollectFourActivity.tableName);
    }

    public void queryData(String tag){
        Log.d(LOG_TAG,"query 4 datas");
        mQueryQueue = new LinkedList<SampleData>();
        mQueryQueue =  mDBM.queryTagData(tableName,tag);
        Log.d(LOG_TAG,"query list size is " + mQueryQueue.size());
    }

   public void addData(String tableName, Queue<SampleData> mQueue){
       Log.d(LOG_TAG,"addData 4 datas");
       mDBM.add(tableName, mQueue);
   }
    public void deleteData(String tableName, String tag){
        Log.d(LOG_TAG,"deleteData");
        mDBM.deleteData(tableName, tag);
    }
    public void updateData(String tableName, String tag, Queue<SampleData> mQueue){
        Log.d(LOG_TAG,"updateData");
        mDBM.updateData(tableName, tag, mQueue);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //dbManager.closeDatabase();
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
