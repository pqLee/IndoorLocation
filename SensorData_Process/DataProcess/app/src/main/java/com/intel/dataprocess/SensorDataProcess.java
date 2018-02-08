package com.intel.dataprocess;

/**
 * 几种常见的简单传感器数据处理算法
 * Created by LiPengqiang on 17-2-24.
 */
public class SensorDataProcess {
    // 加权平均:平滑和均衡传感器数据，减小偶然数据突变的影响；
    // （新值） = （旧值）*（1 - a） + X * a , 其中a为权值,一般比较小，如0.1
    public static float[] WeightedSmooth (float[] data, float alpha)
    {
        float[] newData = new float[3];
        newData[0] = newData[0] * (1.0f - alpha) + data[0] * alpha;
        newData[1] = newData[1] * (1.0f - alpha) + data[1] * alpha;
        newData[2] = newData[2] * (1.0f - alpha) + data[2] * alpha;

        return newData;
    }

    // 抽取突变：去除静态和缓慢变化的数据背景，强调瞬间变化；
    // 采用上面加权平滑的逆算法。 alpha一般比较大，如0.8
    public static float[] ExtractMutations (float[] data, float aplha)
    {
        float[] newData = new float[3];

        return newData;
    }

    // 简单移动平均线：保留数据流最近的K个数据，取平均值
    // 保留传感器数据流中最近的K个数据，返回它们的平均值。k表示平均“窗口”的大小
    public static float[] SimMovAve (float[] data, int k)
    {
        float[] newData = new float[3];

        return newData;
    }
}
































//    private float[] mAccelerometer0 = new float[3];
//    private float[] mAccelerometer1 = new float[3];
//    private float[] mVelocity0 = new float[3];
//    private float[] mVelocity1 = new float[3];
//    private float[] mDistance0 = new float[3];
//    private float[] mDistance1 = new float[3];
//
//
//    private int countX, countY, countZ;
//
//    public SensorDataProcess()
//    {
//        mAccelerometer0 = new float[]{0, 0, 0};
//        mAccelerometer1 = new float[]{0, 0, 0};
//        mVelocity0 = new float[]{0, 0, 0};
//        mVelocity1 = new float[]{0, 0, 0};
//        mDistance0 = new float[]{0, 0, 0};
//        mDistance1 = new float[]{0, 0, 0};
//        countX = 0;
//        countY = 0;
//        countZ = 0;
//    }
//
//    public void LowFilter(float[] sampleValue)
//    {
//        int count = 0;
//
//        do {
//            mAccelerometer1[0] = mAccelerometer1[0] + sampleValue[0];
//            mAccelerometer1[1] = mAccelerometer1[1] + sampleValue[1];
//            mAccelerometer1[2] = mAccelerometer1[2] + sampleValue[2];
//
//            count++;
//        } while (count != 64);
//
//        mAccelerometer1[0] = mAccelerometer1[0] / 64;  // division by 64.
//        mAccelerometer1[1] = mAccelerometer1[1] / 64;
//        mAccelerometer1[2] = mAccelerometer1[2] / 64;
//    }
//
//    public void MechanicalFilterWin (float[] sampleValue, float threshold)
//    {
//        if ((sampleValue[0] <= threshold) && (sampleValue[0] >= -threshold))
//        {
//            sampleValue[0] = 0;
//        }
//    }
//
//    public void MotionCheck(float[] sampleValue, float threshold)
//    {
//        // 判断加速度在一个很小的范围内波动，则判断为静止.
////        if (mAccelerometer1[1] == 0)
////            countX++;
//        /** X axis **/
//        if (sampleValue[0] < threshold && sampleValue[0] > -threshold)
//            countX++;
//        else
//            countX = 0;
//
//        if (countX >= 25)
//        {
//            mVelocity0[0] = 0;
//
//            mVelocity1[0] = 0;
//        }
//        /** Y axis **/
//        if (sampleValue[1] < threshold && sampleValue[1] > -threshold)
//            countY++;
//        else
//            countY = 0;
//
//        if (countY >= 25)
//        {
//            mVelocity0[1] = 0;
//
//            mVelocity1[1] = 0;
//        }
//        /** Z axis **/
//        if (sampleValue[2] < threshold && sampleValue[2] > -threshold)
//            countZ++;
//        else
//            countZ = 0;
//
//        if (countZ >= 25)
//        {
//            mVelocity0[2] = 0;
//
//            mVelocity1[2] = 0;
//        }
//    }
//
//    void Position ()
//    {
//        // First X integration:
//        mVelocity1[0] = mVelocity1[0] + mAccelerometer0[0] + ((mAccelerometer1[0] - mAccelerometer0[0]) / 2);
//        // Second X integration:
//        mDistance1[0] = mDistance0[0] + mVelocity0[0] + ((mVelocity1[0] - mVelocity0[0]) / 2);
//
//        // First Y integration:
//        mVelocity1[1] = mVelocity1[1] + mAccelerometer0[1] + ((mAccelerometer1[1] - mAccelerometer0[1]) / 2);
//        // Second Y integration:
//        mDistance1[1] = mDistance0[1] + mVelocity0[1] + ((mVelocity1[1] - mVelocity0[1]) / 2);
//
//        // First Z integration:
//        mVelocity1[2] = mVelocity1[2] + mAccelerometer0[2] + ((mAccelerometer1[2] - mAccelerometer0[2]) / 2);
//        // Second Y integration:
//        mDistance1[2] = mDistance0[2] + mVelocity0[2] + ((mVelocity1[2] - mVelocity0[2]) / 2);
//
//        //The current acceleration value must be sent to the previous acceleration value
//        mAccelerometer0[0] = mAccelerometer1[0];
//        mAccelerometer0[1] = mAccelerometer1[1];
//        mAccelerometer0[2] = mAccelerometer1[2];
//
//        mDistance0[0] = mAccelerometer0[0];
//        mDistance0[1] = mAccelerometer0[1];
//        mDistance0[2] = mAccelerometer0[2];
//    }
//
//    public float[] getVelocity()
//    {
//        return mVelocity1;
//    }
//
//    public float[] getDistance()
//    {
//        return mDistance1;
//    }