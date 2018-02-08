package com.intel.indoorlocation.SensorDataManager;

import android.util.Log;

import com.intel.indoorlocation.DataManager.MovingAverage;

/**
 * Process the data collect from LinearAccelerometer Sensor which is no "g" influence.
 * Created by lpq on 17-3-9.
 */
public class LinearAccelerometer {
    float deltaTime = 0.006f;
    float threshold1 = 1.5f;    // 当加速度变化超过这个阈值时，才进行积分求速度
    float threshold2 = 0.2f;    // 运动速度的变化范围
    float threshold3 = 0.2f;    // 加速度在零附jin震荡

    private float[] mNewestData = new float[3];    // data collected by linear_accelerometer.
    private float[] mOlderData = new float[3];

    private float[] mVelocity1 = new float[3];
    private float[] mVelocity2 = new float[3];
    private float[] mVelocity3 = new float[3];

    float[] mDeltaAccele = new float[3];
    float[] mDeltaVelocity = new float[3];

    private MovingAverage movingAverage0;
    private MovingAverage movingAverage1;
    private MovingAverage movingAverage2;

    private float[] mDistance1 = new float[3];
    private float[] mDistance2 = new float[3];
    private float[] mDistance3 = new float[3];

    public LinearAccelerometer()
    {
        int NUM = 20;
        movingAverage0 = new MovingAverage(NUM);
        movingAverage1 = new MovingAverage(NUM);
        movingAverage2 = new MovingAverage(NUM);
    }

    private void DataFilter()
    {
        /** X **/
        movingAverage0.pushValue(mNewestData[0]);
        mNewestData[0] = movingAverage0.getValue();
        /** Y **/
        movingAverage1.pushValue(mNewestData[1]);
        mNewestData[1] = movingAverage1.getValue();
        /** Z **/
        movingAverage2.pushValue(mNewestData[2]);
        mNewestData[2] = movingAverage2.getValue();

        /** Data pre_process **/
    }

    public void DistanceCal(float[] data)
    {
        mNewestData = data;
        DataFilter();

        mDeltaAccele[0] = mNewestData[0] - mOlderData[0];

        Log.d("data : " , String.valueOf(mNewestData[0]));
        Log.e("Delta Accele Value :   ", String.valueOf(mDeltaAccele[0]));

        mDeltaVelocity[0] = mVelocity3[0] - mVelocity2[0];

        mOlderData = mNewestData;

        if (mDeltaAccele[0] > threshold2 || mDeltaAccele[0] < -threshold2)
        {
            mVelocity3[0] = 2 * mVelocity2[0] - mVelocity1[0] + mDeltaAccele[0] * deltaTime;   // Calculate the Velocity.


            //2、继续判断：根据速度来进一步判断，在满足1的前提下，若运动速度也随着加速度在一定时间内没有发生太大变化或者在所设定的范围之内，
            //   则可以认为物体运动速度维持在一个数值不变，这时可能是在匀速运动可能是保持静止.
            if (Math.abs(Math.abs(mVelocity3[0] - mVelocity2[0]) - Math.abs(mVelocity2[0] - mVelocity1[0])) < threshold2)  // 两个速度之差的绝对值在阈值范围之内，则认为加速度不变。
            {
                mNewestData[0] = 0;   // 速度在一个小的范围内波动，则加速度置0.
                mDistance3[0] += mVelocity3[0] * deltaTime;
                Log.e("X : ", String.valueOf(mDistance3[0]));
            }
            //3、对于物体运动位移的修正分析：若载体速度在一定所设范围内变化，则认为运动速度不变，同时进一步判断加速度的变化范围
            //   若加速度也在一定范围内变化则物体是匀速运动。
            //   反之是在做加速运动.
        }
        mVelocity2[0] = mVelocity3[0];
        mVelocity1[0] = mVelocity2[0];

        //1、如果加速度变化的绝对值超过了阈值（threshold）则根据以下公式去计算物体的速度与位移
//        if (mDeltaAccele[0] > threshold1 || mDeltaAccele[0] < -threshold1)               // 加速度一直在变化（可能增大或减小）
//        {
//            mVelocity3[0] = 2 * mVelocity2[0] - mVelocity1[0] + mDeltaAccele[0] * deltaTime;
//            mDistance3[0] = 2 * mDistance2[0] - mDistance1[0] + mDeltaVelocity[0] * deltaTime;
//        }
//        else                                                                             // 认为加速度a没有发生显著变化：(1)匀变速 a=不为零常量, (2)匀速 a=0.
//        {
//            //2、继续判断：根据速度来进一步判断设备是在匀速运动还是匀加速运动. 若速度随着加速度在一定时间内没有发生太大变化，则匀速或静止（a=0）.
//            if (Math.abs(Math.abs(mVelocity3[0] - mVelocity2[0]) - Math.abs(mVelocity2[0] - mVelocity1[0])) < threshold2)  // 两个速度之差的绝对值在阈值范围之内,速度按照前一次速度进行匀速运动 ————————————  a = 零常量
//            {
//                //3、进一步判断加速度的变化范围(我设想的，与0接近与否)
//                if (Math.abs(mNewestData[0]) < threshold3)                                                                    // 如果加速度变化范围也在所设定的范围之内————匀速；此时加速度为0)
//                {
//                    mDistance1[0] = mVelocity3[0] * deltaTime;  // 唯一是匀速运动的情况
//                    mNewestData[0] = 0;
//                }
//                else                                                                     // 反之,则按照前面的方法计算位移.
//                {
//                    mVelocity3[0] = 2 * mVelocity2[0] - mVelocity1[0] + mDeltaAccele[0] * deltaTime;
//                    mDistance3[0] = 2 * mDistance2[0] - mDistance1[0] + mDeltaVelocity[0] * deltaTime;
//                }
//            }
//            //2、 速度一直在发生变化————a保持不变，是在做匀加速运动 ———————————— a=不为零常量
//            else
//            {
//                mVelocity3[0] = 2 * mVelocity2[0] - mVelocity1[0] + mDeltaAccele[0] * deltaTime;
//                mDistance3[0] = 2 * mDistance2[0] - mDistance1[0] + mDeltaVelocity[0] * deltaTime;
//            }
//        }
//
//        mVelocity2 = mVelocity3;
//        mVelocity1 = mVelocity2;
//
//        mDistance2 = mDistance3;
//        mDistance1 = mDistance2;
    }


    public float[] GetDistance()
    {
        return mDistance3;
    }
}























