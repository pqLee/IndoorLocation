package com.intel.test.locationdemo.data;

/**
 * Created by lpq on 17-5-25.
 * For fingerprint match.
 */
public class FingerprintMatcher {
    private int label1;
    private int label2;
    private float[] previousData = new float[]{0, 0, 0};

    public FingerprintMatcher()
    {
        label1 = 0;
        label2 = 0;
    }

    /**
     * The core match function. Match the point.
     * @param RawData One item we get from the database. map data structure.
     * @param sampleData The sampled data.
     * @return Return the matched position's id which stored in the map data structure.
     */
    public int PointMatcher(float[][] RawData, float[] sampleData)
    {
        int len = RawData.length;
        //Log.e("Length : ", String.valueOf(len));
        float temp;
        float result1 = 1000000.0f;
        for (int i = 0; i < len; i++)
        {
            if (0 == sampleData[0])
            {
                //Log.e("Loading data......","000");
            }
            else
            {
                temp = Distance(RawData[i], sampleData);
                if (result1 > temp)
                {
                    result1 = temp;
                    //label1 = i;
                    if (result1 > 6.0f)
                    {
                        label1 = 10000;
                    }
                    else {
                        label1 = i;
                    }
                }
            }
        }
        //Log.d("Eucl Distance : ", String.valueOf(result1));
        return label1;
    }

    /**
     * The core match function. Match the region. For instance between K1 and K2.
     * @param RawData One item we get from the database. map data structure.
     * @param sampleData The sampled data.
     * @return Caution: The positioned value is between label2 and label2-1.
     */
    public int RegionMatcher (float[][] RawData, float[] sampleData)
    {
        int len = RawData.length;
        float temp;
        float result2 = 1000000.0f;

        for (int i = 0; i < len; i++)
        {
            if (0 == sampleData[0])
            {
                //Log.e("Loading data......","000");
            }
            else
            {
                temp = (Distance(previousData, sampleData) + Distance(previousData, sampleData));
                if (result2 > temp)
                {
                    result2 = temp;
                    label2 = i;
                }
            }
        }
        previousData = sampleData;
        return label2;
    }

    private float Distance(float[] MagData, float[] sampleData)
    {
        float distance;
        distance = (float) Math.sqrt(
                (MagData[0] - sampleData[0]) * (MagData[0] - sampleData[0]) +
                        (MagData[1] - sampleData[1]) * (MagData[1] - sampleData[1]) +
                        (MagData[2] - sampleData[2]) * (MagData[2] - sampleData[2])
        );
        return distance;
    }
}













