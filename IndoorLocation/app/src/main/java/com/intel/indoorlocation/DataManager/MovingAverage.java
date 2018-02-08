package com.intel.indoorlocation.DataManager;

/**
 * 保留数据流最近的K个数据，取平均值
 * 保留传感器数据流中最近的K个数据，返回它们的平均值。k表示平均“窗口”的大小
 * Created by LiPengqiang on 17-3-9.
 */
public class MovingAverage {
    private float circularBuffer[];
    private float avg;
    private float sum;
    private int circularIndex;
    private int count;

    public MovingAverage(int k){
        circularBuffer = new float[k];
        count = 0;
        circularIndex = 0;
        avg = 0;
        sum = 0;
    }

    public float getValue(){
        return avg;
    }

    private int nextIndex(int curIndex){
        if(curIndex + 1 >= circularBuffer.length){
            return 0;
        }
        return curIndex + 1;
    }

    public void pushValue(float x){      //The newest value of sensor value.
        if(count++ == 0)
        {
            for (int i = 0; i < circularBuffer.length; i++)
            {
                circularBuffer[i] = x;
                sum += x;
            }
        }
        float lastValue = circularBuffer[circularIndex];
        circularBuffer[circularIndex] = x;    // 更新窗口中传感器数据
        sum -= lastValue;                     // 更新窗口中传感器数据和
        sum += x;
        avg = sum / circularBuffer.length;    // 计算得传感器平均值

        circularIndex = nextIndex(circularIndex);
    }
}
