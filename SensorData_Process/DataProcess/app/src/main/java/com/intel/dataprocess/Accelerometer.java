package com.intel.dataprocess;

/**
 * Created by LiPengqiang on 17-3-9.
 * 对加速度传感器数据处理：滤波、积分
 */
public class Accelerometer {
    private float[] AcceleData;

    public Accelerometer(float[] data)
    {
        AcceleData = data;
    }
}
