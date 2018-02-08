package com.intel.indoorlocation.SensorDataManager;

import com.intel.indoorlocation.DataManager.MovingAverage;

/**
 * Process the data collect from Accelerometer Sensor.
 * Created by lpq on 17-3-9.
 */
public class Accelerometer {
    private float[] mAccelerometerData;
    private static int NUM = 20;

    private MovingAverage movingAverage0;
    private MovingAverage movingAverage1;
    private MovingAverage movingAverage2;

    public Accelerometer(float[] data)
    {
        mAccelerometerData = data;

        movingAverage0 = new MovingAverage(NUM);
        movingAverage1 = new MovingAverage(NUM);
        movingAverage2 = new MovingAverage(NUM);
    }

    public void DataFilter()
    {
        /** X **/
        movingAverage0.pushValue(mAccelerometerData[0]);
        mAccelerometerData[0] = movingAverage0.getValue();
        /** Y **/
        movingAverage1.pushValue(mAccelerometerData[1]);
        mAccelerometerData[1] = movingAverage1.getValue();
        /** Z **/
        movingAverage2.pushValue(mAccelerometerData[2]);
        mAccelerometerData[2] = movingAverage2.getValue();
    }

}
