package com.intel.sensorinteraction;

/**
 * Created by lpq on 17-8-7.
 * Calculate the angle changed value by kalman.
 */
public class AngleChange {

    /**
     *
     * @param accele The accelerometer value from sensor
     * @param dir 0:angle with z-axis, 1:angle with x-axis, 2:angle with y-axis
     * @return The Angle value calculate by accelerometer value
     * theta = arctan(ACCELEx / ACCELEz)
     */
    public static float angleFromAccele(float[] accele, int dir)
    {
        float angle = 0;
        float temp;

        switch (dir)
        {
            case 0:
                temp = (float)Math.sqrt((accele[0] * accele[0] + accele[1] * accele[1])) / accele[2];
                angle = (float)Math.atan(temp);
                angle = (float) Math.toDegrees(angle);
                break;
            case 1:
                temp = (float)Math.sqrt((accele[1] * accele[1] + accele[2] * accele[2])) / accele[2];
                angle = (float)Math.atan(temp);
                break;
            case 2:
                temp = (float)Math.sqrt((accele[2] * accele[2] + accele[0] * accele[0])) / accele[2];
                angle = (float)Math.atan(temp);
                break;
        }

        return angle;
    }

    /**
     *
     * @param gyro The gyroscope value from sensor
     * @return
     */
    public float angleFromGyro(float[] gyro)
    {
        float angle = 0;

        return angle;
    }

    /**
     * Actual angle calculate by data fusion ---- Kalman Filter.
     * @return The actual angle that device changed
     */
    public static float angleFormFusion()
    {
        float angle = 0;

        return angle;
    }
}
