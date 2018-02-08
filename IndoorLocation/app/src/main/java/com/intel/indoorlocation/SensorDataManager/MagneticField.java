package com.intel.indoorlocation.SensorDataManager;

import android.hardware.SensorManager;

import com.intel.indoorlocation.DataManager.MatrixManager;
import com.intel.indoorlocation.DataManager.MovingAverage;

/**
 * Created by lpq on 17-3-9.
 * magnetic-field data axis transform.
 */


public class MagneticField {
    private static MovingAverage movingAverage0 = new MovingAverage(10);
    private static MovingAverage movingAverage1 = new MovingAverage(10);
    private static MovingAverage movingAverage2 = new MovingAverage(10);
    private static float[] rotate = new float[16];
    private static float[] remapR = new float[16];
    private static float[] angleInRad = new float[3];


    public static float[] MagneticDataProcess(float[] mMagneticData, float[] mAcceleData)
    {
        movingAverage0.pushValue(mMagneticData[0]);
        mMagneticData[0] = movingAverage0.getValue();
        movingAverage1.pushValue(mMagneticData[1]);
        mMagneticData[1] = movingAverage1.getValue();
        movingAverage2.pushValue(mMagneticData[2]);
        mMagneticData[2] = movingAverage2.getValue();

        GetOrientation(mAcceleData, mMagneticData);
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
        float[] magIni = new float[]{mMagneticData[0], mMagneticData[1], mMagneticData[2]};
        float[] magRot = MatrixManager.getMultiVector(Rzyx, magIni);
        return magRot;
    }

    private static void GetOrientation(float[] mAcceleData, float[] mMagneticData)
    {
        SensorManager.getRotationMatrix(rotate, null, mAcceleData, mMagneticData);
        SensorManager.remapCoordinateSystem(rotate, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remapR);
        SensorManager.getOrientation(remapR, angleInRad);
    }
}
