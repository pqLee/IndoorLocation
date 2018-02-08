package com.intel.dataprocess;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private static final int SAMPLE_COUNT = 40;
    private static final int SAMPLE_ORIENTATIONS = 4;
    private DecimalFormat df;
    private SensorManager mSensorManager;
    private AcceleSensorListener mySensorEvent;
    private TextView mPositionView;
    private float[] mAcceValue1 = new float[3];
    private float[] mAcceValue2 = new float[3];
    private float[] mDeltaAccele = new float[3];
    private float[] mAccelerometerData = new float[3];    //加速度计的原始数据
    private float[] mVelocity1 = new float[3];
    private float[] mVelocity2 = new float[3];
    private float[] mVelocity3 = new float[3];
    private float[] mDistance = new float[3];
    private float[] mGyroValue = new float[3];
    private float[] mMagneticValue = new float[3];        // 地磁传感器的原始数据
    private float[] mAngle = new float[3];  // 弧度
    private String[] angle = new String[3];   // 角度
    private static final float NS2s = 1.0f / 1000000000.0f;   // 将纳秒转化为秒.
    private float timeStamp;
    private float[] rotation = new float[3];
    private String textShow;   // 显示在屏幕上的字符串
    private float[] valuesInRad = new float[3];
    private float[] valuesInDeg = new float[3];
    private float[] rotate = new float[16];
    private float[] remapR = new float[16];
    private float[] MagIni = new float[3];
    private float[] MagRot1 = new float[3];
    private float[] MagRot2 = new float[3];
    private float[] MagRot3 = new float[3];
    private float[] MagRot4 = new float[3];
    private float[] MagRot5 = new float[3];
    private float[] MagRot6 = new float[3];
    private float[][] Rz = new float[3][3];
    private float[][] Rx = new float[3][3];
    private float[][] Ry = new float[3][3];
    private float[][] Rxyz1 = new float[3][3];
    private float[][] Rxyz2 = new float[3][3];
    private float[][] Rxyz3 = new float[3][3];
    private float[][] Rxyz4 = new float[3][3];
    private float[][] Rxyz5 = new float[3][3];
    private float[][] Rxyz6 = new float[3][3];
    private int count = 0;
    private int countDatas = 0;
    private float[][] TrainingSample = new float[SAMPLE_COUNT][3];   // 保存训练样本的初始数据。以d为间隔按顺序选取N(25)个位置，记录下P(4)个不同方位对应的地磁强度测量值. 4*25=100
    private float[][] TrainingDataProcessed = new float[SAMPLE_COUNT / SAMPLE_ORIENTATIONS][3];  // 保存训练样本处理后的均值.此为真正需要的“指纹库”
    private float[][] TestSample = new float[SAMPLE_COUNT][3];        // 测试集：保存测试样本的初始数据。在吧不同于以上N个位置的地方选取M个位置，记录下Q个不同方位对应的地磁强度测量值。

    private Button SampleDataBtn;
    private TextView MagDataTextView;
    private TextView MagDataProcessView;

    private MovingAverage movingAverage1;
    private MovingAverage movingAverage2;
    private MovingAverage movingAverage3;
    private MovingAverage movingAverage4;
    private MovingAverage movingAverage5;
    private MovingAverage movingAverage6;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPositionView = (TextView) findViewById(R.id.positionView);

        mySensorEvent = new AcceleSensorListener();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor mAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor mAccelerSensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(mySensorEvent, mAcceleSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mySensorEvent, mAccelerSensor2, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mySensorEvent, mGyroSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mySensorEvent, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);

        SampleDataBtn = (Button) findViewById(R.id.DataSample);
        SampleDataBtn.setOnClickListener(new SampleDataListener());
        MagDataTextView = (TextView) findViewById(R.id.Mag_data);
        MagDataProcessView = (TextView) findViewById(R.id.Processed_data);
        Button dataProcessedBtn = (Button) findViewById(R.id.DataProcess);
        dataProcessedBtn.setOnClickListener(new SampleDataProcessListener());

        movingAverage1 = new MovingAverage(20);
        movingAverage2 = new MovingAverage(20);
        movingAverage3 = new MovingAverage(20);
        movingAverage4 = new MovingAverage(20);
        movingAverage5 = new MovingAverage(20);
        movingAverage6 = new MovingAverage(20);

        df = new DecimalFormat("#0.0000");
        df.getRoundingMode();


        MatrixText();
    }

    private final Object mObjectLock = new Object();


    private class AcceleSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                mAcceValue2 = event.values;  // 单位： m/s*s

                LinearAccelerometerDataProcess();

                //mPositionView.setText("distance : " + mDistance[0]);
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccelerometerData = event.values;

                //AccelerometerDataProcess();
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if (timeStamp != 0) {
                    final float dT = (event.timestamp - timeStamp) * NS2s;    // 计算两次检测手机旋转的时间差（纳秒），并将其转化成秒
                    mGyroValue = event.values;
                    // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
                    mAngle[0] += mGyroValue[0] * dT;
                    mAngle[1] += mGyroValue[1] * dT;
                    mAngle[2] += mGyroValue[2] * dT;

                    GyroscopeDataProcess();
                }
                timeStamp = event.timestamp;    // 将当前的时间赋给timeStamp.
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // 三个方向上的电磁强度，单位是微特斯拉(micro-Tesla),用uT表示，也可以是高斯(Gauss), 1Tesla = 10000Gauss.
                mMagneticValue = event.values;
                //MagneticFieldDataProcess();
            }

            //MagneticRotation();

//            TextShow();
//            mPositionView.setText(textShow);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private void GyroscopeDataProcess() {
        // 速弧度转角度
        angle[0] = df.format(Math.toDegrees(mAngle[0]));
        angle[1] = df.format(Math.toDegrees(mAngle[1]));
        angle[2] = df.format(Math.toDegrees(mAngle[2]));
    }

    private void LinearAccelerometerDataProcess() {
        /*********** Data pre_process *************/
        mDeltaAccele[0] = mAcceValue2[0] - mAcceValue1[0];   // Newest - Older
        mDeltaAccele[1] = mAcceValue2[1] - mAcceValue1[1];
        mDeltaAccele[2] = mAcceValue2[2] - mAcceValue1[2];

        Log.e("data : ", String.valueOf(mDeltaAccele[0]));

        mAcceValue1[0] = mAcceValue2[0];
        mAcceValue1[1] = mAcceValue2[1];
        mAcceValue1[2] = mAcceValue2[2];

        //1、如果加速度变化的绝对值超过了阈值（threshold）则根据以下公式去计算物体的速度与位移
        /** X **/
        float threshold1 = 1.5f;
        float threshold2 = 0.2f;
        float deltaTime = 0.06f;
        if (mDeltaAccele[0] > threshold1 || mDeltaAccele[0] < -threshold2) {
            mVelocity3[0] = 2 * mVelocity2[0] - mVelocity1[0] + mDeltaAccele[0] * deltaTime;   // Calculate the Velocity.
            //2、继续判断：根据速度来进一步判断，在满足1的前提下，若运动速度也随着加速度在一定时间内没有发生太大变化或者在所设定的范围之内，
            //  则可以认为物体运动速度维持在一个数值不变，这时可能是在匀速运动可能是保持静止.
            if (Math.abs(Math.abs(mVelocity3[0] - mVelocity2[0]) - Math.abs(mVelocity2[0] - mVelocity1[0])) < threshold2)  // 两个速度之差的绝对值在阈值范围之内，则认为加速度不变。
            {
                mAcceValue2[0] = 0;   // 速度在一个小的范围内波动，则加速度置0.
                mDistance[0] += mVelocity3[0] * deltaTime;
            }
            //3、对于物体运动位移的修正分析：若载体速度在一定所设范围内变化，则认为运动速度不变，同时进一步判断加速度的变化范围
            //   若加速度也在一定范围内变化则物体是匀速运动。
            //   反之是在做加速运动.
        }
        mVelocity2[0] = mVelocity3[0];
        mVelocity1[0] = mVelocity2[0];

        /** Y **/
        if (mDeltaAccele[1] > threshold1 || mDeltaAccele[1] < -threshold2) {
            mVelocity3[1] = 2 * mVelocity2[1] - mVelocity1[1] + mDeltaAccele[1] * deltaTime;

            if (Math.abs(Math.abs(mVelocity3[1] - mVelocity2[1]) - Math.abs(mVelocity2[1] - mVelocity1[1])) < threshold2) {
                mAcceValue2[1] = 0;
                mDistance[1] += mVelocity3[1] * deltaTime;
            }
        }
        mVelocity2[1] = mVelocity3[1];
        mVelocity1[1] = mVelocity2[1];
    }

    private void AccelerometerDataProcess() {
        mAccelerometerData = SensorDataProcess.WeightedSmooth(mAccelerometerData, 0.1f);
        double g = Math.sqrt(mAccelerometerData[0] * mAccelerometerData[0] + mAccelerometerData[1] *
                mAccelerometerData[1] + mAccelerometerData[2] * mAccelerometerData[2]);

        double cosAx = mAccelerometerData[0] / g;
        double cosAy = mAccelerometerData[1] / g;
        double cosAz = mAccelerometerData[2] / g;

        rotation[0] = (float) cosTorad(cosAx, mAccelerometerData[0]);
        rotation[1] = (float) cosTorad(cosAy, mAccelerometerData[1]);
        rotation[2] = (float) cosTorad(cosAz, mAccelerometerData[2]);

        movingAverage4.pushValue(mAccelerometerData[0]);
        mAccelerometerData[0] = movingAverage4.getValue();
        mAccelerometerData[0] = Float.parseFloat(df.format(mAccelerometerData[0]));

        movingAverage5.pushValue(mAccelerometerData[1]);
        mAccelerometerData[1] = movingAverage5.getValue();
        mAccelerometerData[1] = Float.parseFloat(df.format(mAccelerometerData[1]));

        movingAverage6.pushValue(mAccelerometerData[2]);
        mAccelerometerData[2] = movingAverage6.getValue();
        mAccelerometerData[2] = Float.parseFloat(df.format(mAccelerometerData[2]));
    }

    private void MagneticFieldDataProcess() {
        mMagneticValue = SensorDataProcess.WeightedSmooth(mMagneticValue, 0.1f);
        movingAverage1.pushValue(mMagneticValue[0]);
        mMagneticValue[0] = movingAverage1.getValue();
        mMagneticValue[0] = Float.parseFloat(df.format(mMagneticValue[0]));

        movingAverage2.pushValue(mMagneticValue[1]);
        mMagneticValue[1] = movingAverage2.getValue();
        mMagneticValue[1] = Float.parseFloat(df.format(mMagneticValue[1]));

        movingAverage3.pushValue(mMagneticValue[2]);
        mMagneticValue[2] = movingAverage3.getValue();
        mMagneticValue[2] = Float.parseFloat(df.format(mMagneticValue[2]));
    }

    private void MagneticRotation() {
        GetOrientation();
        // 3个旋转矩阵
        Rz = new float[][]{{(float) Math.cos(valuesInRad[0]), (float) -Math.sin(valuesInRad[0]), 0},
                {(float) Math.sin(valuesInRad[0]), (float) Math.cos(valuesInRad[0]), 0},
                {0, 0, 1}};

        Rx = new float[][]{{1, 0, 0},
                {0, (float) Math.cos(valuesInRad[1]), (float) -Math.sin(valuesInRad[1])},
                {0, (float) Math.sin(valuesInRad[1]), (float) Math.cos(valuesInRad[1])}};

        Ry = new float[][]{{(float) Math.cos(valuesInRad[2]), 0, (float) -Math.sin(valuesInRad[2])},
                {0, 1, 0},
                {(float) Math.sin(valuesInRad[2]), 0, (float) Math.cos(valuesInRad[2])}};

        // 求 Rx*Ry*Rz
        float[][] Rxy = MatrixManager.getMultiply(Rz, Ry);
        //Log.d("data : ", String.valueOf(Ryx[0][0]));
        Rxyz1 = MatrixManager.getMultiply(Rxy, Rx);
        //Log.e("data : ", String.valueOf(Rxyz[0][0]));
        // 求旋转矩阵的逆矩阵
        Rxyz1 = MatrixManager.getNi3(Rxyz1);
        // 设备测量的磁场强度
        MagIni = new float[]{mMagneticValue[0], mMagneticValue[1], mMagneticValue[2]};
        // 求坐标转换以后的X Y Z轴磁场数据（最终的结果）
        synchronized (mObjectLock) {
            MagRot1 = MatrixManager.getMultiVector(Rxyz1, MagIni);
        }
        // 对于训练样本数据采集到的N个位置、P个方位的数据，建立指纹库：第i个位置、第j个方位的测量值求平均值。

        if (count != 30) {
            count++;
        } else {
            mPositionView.setText("Rz * Ry * Rx" + "\n" + "raw_data : \n" + "X : " + mMagneticValue[0] + "\n" + "Y : " + mMagneticValue[1] + "\n" + "Z : " + mMagneticValue[2] + "\n" + "\n"
                    + "\n" + "processed_data : \n" + MagRot1[0] + "  " + MagRot1[1] + "  " + MagRot1[2] + "\n\n\n" +
                    "angle : \n" + "X : " + valuesInDeg[0] + "\n" + "Y : " + valuesInDeg[1] + "\n" + "Z : " + valuesInDeg[2] + "\n"
                    + "\n");
            count = 0;
        }
    }

    private void GetOrientation() {
        SensorManager.getRotationMatrix(rotate, null, mAccelerometerData, mMagneticValue);
        SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Z,
                SensorManager.AXIS_MINUS_X, remapR);   // 机身坐标系mR和世界坐标系remap的映射
        SensorManager.getOrientation(remapR, valuesInRad);   // 在世界坐标系里，机器绕Z轴、X轴、Y轴旋转的角度。对应α、β、γ.

        valuesInDeg[0] = (float) Math.toDegrees(valuesInRad[0]);  // Azimuth  Z axis
        valuesInDeg[1] = (float) Math.toDegrees(valuesInRad[1]);  // Pitch    X axis
        valuesInDeg[2] = (float) Math.toDegrees(valuesInRad[2]);  // Roll     Y axis

        //mPositionView.setText("angle : \n" + "X : " + valuesInDeg[0] + "\n" + "Y : " + valuesInDeg[1] + "\n" + "Z : " + valuesInDeg[2] + "\n");
    }

    private class SampleDataListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            synchronized (mObjectLock) {
                if (countDatas < SAMPLE_COUNT) {
                    TrainingSample[countDatas][0] = MagRot1[0];
                    TrainingSample[countDatas][1] = MagRot1[1];
                    TrainingSample[countDatas][2] = MagRot1[2];
                    SampleDataBtn.setText("第" + (countDatas + 1) + "个数据");
                    MagDataTextView.setText("采样数据：" + TrainingSample[countDatas][0] + " " + TrainingSample[countDatas][1] + " " + TrainingSample[countDatas][2]);
                    countDatas++;
                } else {
                    Toast.makeText(MainActivity.this, "数据采集完成！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class SampleDataProcessListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (countDatas == SAMPLE_COUNT) {
                int count = 0;
                Float sumX = 0.0f;
                Float sumY = 0.0f;
                Float sumZ = 0.0f;
                for (int i = 0; i < SAMPLE_COUNT; i++) {
                    if (count != SAMPLE_ORIENTATIONS) {
                        sumX += TrainingSample[i][0];
                        sumY += TrainingSample[i][1];
                        sumZ += TrainingSample[i][2];
                        count++;
                    } else {
                        TrainingDataProcessed[((i + 1) / 4 - 1)][0] = sumX / SAMPLE_ORIENTATIONS;
                        TrainingDataProcessed[((i + 1) / 4 - 1)][1] = sumY / SAMPLE_ORIENTATIONS;
                        TrainingDataProcessed[((i + 1) / 4 - 1)][2] = sumZ / SAMPLE_ORIENTATIONS;
                        count = 0;
                        sumX = 0.0f;
                        sumY = 0.0f;
                        sumZ = 0.0f;
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "数据采集未完成！", Toast.LENGTH_SHORT).show();
            }
            MagDataProcessView.setText("数据均值：" + TrainingDataProcessed[0][0] + " " + TrainingDataProcessed[0][1] +
                    " " + TrainingDataProcessed[0][2]);
        }
    }

    // 将采集到的即时数据与指纹库中的数据比较，返回与指纹库中欧式距离最小距离对应的点————"i".
    private int FingerprintComparison(float[][] DataBase, float[] InstanceData) {
        float minDistance = 1000000.0f;
        int flag = DataBase.length + 1;   // Default 的flag的值是比database数据量大1，若返回这个，则视为无效数据。
        for (int i = 0; i < DataBase.length; i++) {
            if (EuclDistance(DataBase[i], InstanceData) < minDistance) {
                minDistance = EuclDistance(DataBase[i], InstanceData);
                flag = i;
            }
        }
        return flag;
    }

    private float EuclDistance(float[] SampleData, float[] InstanceData) {
        float distance = 0;
        distance = (float) Math.sqrt(SampleData[0] * InstanceData[0] + SampleData[1] * InstanceData[1] + SampleData[2] * InstanceData[2]);
        return distance;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mySensorEvent);
        super.onPause();
    }

    private double cosTorad(double cos, float value) {
        if (cos > 1)
            cos = 1;
        else if (cos < -1)
            cos = -1;
        double rad = Math.acos(cos);
        rad = Math.toDegrees(rad);
        return rad;
    }

    private void MatrixText() {
        float vector[] = {1, 2, 3};
        float matrix[][] = {{1, 2, 3}, {2, 2, 1}, {3, 4, 3}};
        float matrix1[][] = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        float result[][] = MatrixManager.getMultiply(matrix, matrix1);

        mPositionView.setText("Result : " + "\n"  + result[0][0] +"  " + result[0][1] + "  " + result[0][2]
                + "\n" + result[1][0] +"  " + result[1][1] + "  " + result[1][2]
                + "\n" + result[2][0] +"  " + result[2][1] + "  " + result[2][2]);
        // 经验证，矩阵乘法、求逆、矩阵乘以向量计算是正确的.
    }
}




















//            // 求 Rx*Rz*Ry
//            float[][] Rxz = MatrixManager.getMultiply(Rx, Rz);
//            //Log.d("data : ", String.valueOf(Ryx[0][0]));
//            Rxyz2 = MatrixManager.getMultiply(Rxz, Ry);
//            //Log.e("data : ", String.valueOf(Rxyz[0][0]));
//            // 求旋转矩阵的逆矩阵
//            Rxyz2 = MatrixManager.getNi3(Rxyz2);
//            // 求坐标转换以后的X Y Z轴磁场数据（最终的结果）
//            synchronized (mObjectLock) {
//                MagRot2 = MatrixManager.getMultiVector(Rxyz2, MagIni);
//            }
//
//            // 求 Ry*Rx*Rz
//            float[][] Ryx = MatrixManager.getMultiply(Ry, Rx);
//            //Log.d("data : ", String.valueOf(Ryx[0][0]));
//            Rxyz3 = MatrixManager.getMultiply(Ryx, Rz);
//            //Log.e("data : ", String.valueOf(Rxyz[0][0]));
//            // 求旋转矩阵的逆矩阵
//            Rxyz3 = MatrixManager.getNi3(Rxyz3);
//            // 求坐标转换以后的X Y Z轴磁场数据（最终的结果）
//            synchronized (mObjectLock) {
//                MagRot3 = MatrixManager.getMultiVector(Rxyz3, MagIni);
//            }
//
//            // 求 Ry*Rz*Rx
//            float[][] Ryz = MatrixManager.getMultiply(Ry, Rz);
//            //Log.d("data : ", String.valueOf(Ryx[0][0]));
//            Rxyz4 = MatrixManager.getMultiply(Ryz, Rx);
//            //Log.e("data : ", String.valueOf(Rxyz[0][0]));
//            // 求旋转矩阵的逆矩阵
//            Rxyz4 = MatrixManager.getNi3(Rxyz4);
//            // 求坐标转换以后的X Y Z轴磁场数据（最终的结果）
//            synchronized (mObjectLock) {
//                MagRot4 = MatrixManager.getMultiVector(Rxyz4, MagIni);
//            }
//
//            // 求 Rz*Rx*Ry
//            float[][] Rzx = MatrixManager.getMultiply(Rz, Rx);
//            //Log.d("data : ", String.valueOf(Ryx[0][0]));
//            Rxyz5 = MatrixManager.getMultiply(Rzx, Ry);
//            //Log.e("data : ", String.valueOf(Rxyz[0][0]));
//            // 求旋转矩阵的逆矩阵
//            Rxyz5 = MatrixManager.getNi3(Rxyz5);
//            // 求坐标转换以后的X Y Z轴磁场数据（最终的结果）
//            synchronized (mObjectLock) {
//                MagRot5 = MatrixManager.getMultiVector(Rxyz5, MagIni);
//            }
//
//            // 求 Rz*Ry*Rx
//            float[][] Rzy = MatrixManager.getMultiply(Rz, Ry);
//            //Log.d("data : ", String.valueOf(Ryx[0][0]));
//            Rxyz6 = MatrixManager.getMultiply(Ryx, Rx);
//            //Log.e("data : ", String.valueOf(Rxyz[0][0]));
//            // 求旋转矩阵的逆矩阵
//            Rxyz6 = MatrixManager.getNi3(Rxyz6);
//            // 求坐标转换以后的X Y Z轴磁场数据（最终的结果）
//            synchronized (mObjectLock) {
//                MagRot6 = MatrixManager.getMultiVector(Rxyz6, MagIni);
//            }































/** Test the matrix function **/
//    private float[][] testMatrix1 = new float[3][3];
//    private float[][] testMatrix2 = new float[3][3];
//    private float[][] resultMatrix = new float[3][3];
//    private float[] testVector = new float[3];
//    private float[] resultVector = new float[3];

//testMatrix1 = new float[][]{ {1, -1, 3}, {2, -1, 4}, {-1, 2, -4} };
//        testMatrix2 = new float[][]{ {0, 0, 2}, {7, 5, 0}, {2, 1, 1} };
//        testVector = new float[]{1, 2, 3};
//        resultMatrix = MatrixManager.getNi3(testMatrix1);
//        resultVector = MatrixManager.getMultiVector(testMatrix1, testVector);
//        mPositionView.setText(resultMatrix[0][0] + " " + resultMatrix[0][1] + " " + resultMatrix[0][2] + "\n"
//        + resultMatrix[1][0] + " " + resultMatrix[1][1] + " " + resultMatrix[1][2] + "\n"
//        + resultMatrix[2][0] + " " + resultMatrix[2][1] + " " + resultMatrix[2][2] + "\n" + "\n"
//        + resultVector[0] + " " + resultVector[1] + " " + resultVector[2]);







// mPositionView.setText("X : " + values[0] + "\n" + "Y : " + values[1] + "\n" + "Z : " + values[2] + "\n");






//    private void MotionChecked()
//    {
//        if (mAcceValue[0] < threshold && mAcceValue[0] > -threshold)
//            countX++;
//        else
//            countX = 0;
//        if (countX >= amount)
//        {
//            mAcceValue[0] = 0;
//
//            mAcceValue[0] = 0;
//        }
//
//        if (mAcceValue[1] < threshold && mAcceValue[1] > -threshold)
//            countY++;
//        else
//            countY = 0;
//        if (countY >= amount)
//        {
//            mAcceValue[1] = 0;
//
//            mAcceValue[1] = 0;
//        }
//
//        if (mAcceValue[2] < threshold && mAcceValue[2] > -threshold)
//            countZ++;
//        else
//            countZ = 0;
//
//        if (countZ >= amount)
//        {
//            mAcceValue[2] = 0;
//
//            mAcceValue[2] = 0;
//        }
//    }



















//                mVelocity3[0] += event.values[0] * 0.2f;
//                mVelocity3[1] += event.values[1] * 0.2f;
//                mVelocity3[2] += event.values[2] * 0.2f;
//
//                mDistance[0] += mVelocity3[0] * 0.2f;
//                mDistance[1] += mVelocity3[1] * 0.2f;
//                mDistance[2] += mVelocity3[2] * 0.2f;