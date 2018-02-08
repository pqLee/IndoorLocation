package com.intel.sensorinteraction;

/**
 * Created by lpq on 17-7-27.
 * Data smooth by Simple Moving Average.
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
