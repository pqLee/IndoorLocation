package com.intel.test.locationdemo.data;

/**
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
        circularBuffer[circularIndex] = x;
        sum -= lastValue;
        sum += x;
        avg = sum / circularBuffer.length;

        circularIndex = nextIndex(circularIndex);
    }
}
