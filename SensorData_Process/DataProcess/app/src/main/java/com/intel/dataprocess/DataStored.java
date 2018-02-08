package com.intel.dataprocess;

/**
 * Create a space to store the collected magnetic_field data.
 * SharedPreferences
 * Created by lpq on 17-2-27.
 */
public class DataStored {

    public String FloatToString (float[] data)
    {
        for (int i = 0; i < data.length; i++)
        {
            data[i] = FloatDecimal(data[i]);
        }
        String str = null;
        str += data[0];
        str += "#";
        str += data[1];
        str += "#";
        str += data[2];

        return str;
    }

    public float[] StringToFloat (String str)
    {
        float[] data = new float[3];
        String[] sourceStrArray = str.split("#");
        for (int i = 0; i < sourceStrArray.length; i++)
        {
            data[i] = Float.parseFloat(sourceStrArray[i]);
        }

        return data;
    }

    public float FloatDecimal (float data)
    {
        float b = (float)(Math.round(data * 10000)) / 10000;

        return b;
    }
}
