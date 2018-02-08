package com.intel.sensorinteraction;

import android.util.Log;

/**
 * Created by lpq on 17-7-26.
 *
 */
public class CalDistance {
    private float[] acceleValues = new float[3];
    private float[] velocityValues = new float[3];
    private float[] distanceValues = new float[3];
    private float[] preAcceleValue = new float[3];    // 保存上一个加速度数据.
    private float[] preVelocityValue = new float[3];  // 保存上一个速度数据.
    private float[] preDistanceValue = new float[3];  // 保存上一个距离数据.
    private float T = 0.06f;
    private MovingAverage movingAverage0;

    public CalDistance()
    {
        movingAverage0 = new MovingAverage(10);
    }


    public void setAcceValues(float[] sensorValues)
    {
        System.arraycopy(sensorValues, 0, acceleValues, 0, 3);
    }

    // 通过"矩形+三角形"的积分方式来减小积分误差
    public float[] acceleIntegrate1()
    {
        movingAverage0.pushValue(acceleValues[0]);
        acceleValues[0] = movingAverage0.getValue();
        if (acceleValues[0] < 1.0f && acceleValues[0] > -1.0f)    // mechanical filter window
        {
            acceleValues[0] = 0;
        }
        //Log.d("A : ", String.valueOf(acceleValues[0]));

        velocityValues[0] = preVelocityValue[0] + (acceleValues[0] +
                (Math.abs(acceleValues[0] - preAcceleValue[0])) / 2) * T;
        //Log.d("V : ", String.valueOf(velocityValues[0]));

        distanceValues[0] = preDistanceValue[0] + (velocityValues[0] +
                (Math.abs(velocityValues[0] - preVelocityValue[0])) / 2) * T;

        MotionCheckByAcceData(acceleValues, 0);
        preAcceleValue[0] = acceleValues[0];
        preVelocityValue[0] = velocityValues[0];
        preDistanceValue[0] = distanceValues[0];
        return distanceValues;
    }

    // 通过最原始的"加速度差\速度差\距离差"的方法来减小积分误差
    public void acceleIntegrate2()
    {
        acceleValues[0] = 0.1f * acceleValues[0] + 0.9f * acceleValues[0];
        acceleValues[1] = 0.1f * acceleValues[1] + 0.9f * acceleValues[1];
        acceleValues[2] = 0.1f * acceleValues[2] + 0.9f * acceleValues[2];

        float[] deltaAcceleValue = new float[3];
        float[] deltaVelocityValue = new float[3];

        deltaAcceleValue[0] = acceleValues[0] - preAcceleValue[0];
        deltaVelocityValue[0] = velocityValues[0] - preVelocityValue[0];

        preAcceleValue[0] = acceleValues[0];
        preVelocityValue[0] = velocityValues[0];
    }

    static int count = 0;
    private void MotionCheckByAcceData (float[] acceData, int flag)         // ＂移动结束＂检查，静止时返回true
    {
        if (acceData[flag] == 0)
        {
            count++;
        }
        else {
            count = 0;
        }
        if (count >= 5)   // If this number exceeds 5, we can assume that velocity is zero.
        {
            velocityValues[0] = 0;
            velocityValues[1] = 0;
            velocityValues[2] = 0;
        }
    }

}
