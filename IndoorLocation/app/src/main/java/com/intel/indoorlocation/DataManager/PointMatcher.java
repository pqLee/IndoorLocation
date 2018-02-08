package com.intel.indoorlocation.DataManager;

import android.util.Log;

/**
 * Created by lpq on 17-5-25.
 * Based on Eurl Distance and K-nearest alg to find a nearest point.
 */
public class PointMatcher {
    private int label1;
    private int label2;

    public PointMatcher()
    {
        label1 = -1;
        label2 = -1;
    }

    /**
     * The core match function. Match the point.
     * @param RawData One item we get from the database. map data structure.
     * @param sampleData The sampled data.
     * @return Return the matched position's id which stored in the map data structure.
     */
    public int Matcher(float[][] RawData, float[] sampleData)
    {
        int len = RawData.length;
        float temp;
        float result1 = 1000000.0f;
        for (int i = 0; i < len; i++)
        {
            if (0 == sampleData[0])
            {
                //Log.d("Loading data......","000");
            }
            else
            {
                temp = Distance(RawData[i], sampleData);      // the eucl distance of two datas.
                if (result1 > temp)
                {
                    result1 = temp;
                    //label1 = i;
                    if (result1 > 10.0f)        // the threshold of max distance which located.
                        label1 = -1;
                    else
                        label1 = i;
                }
            }
        }
        return label1;
    }

    /**
     * The core match function. Match the region. For instance between K1 and K2.
     * @param DBData One item we get from the database. map data structure.
     * @param sampleData The sampled data.
     * @return Caution: The positioned value is between label2 and label2-1.
     */
    public int RegionMatcher (float[][] DBData, float[] sampleData)
    {
        int len = DBData.length;
        float temp;
        float result2 = 1000000.0f;

        for (int i = 0; i < len; i++)
        {
            if (0 == sampleData[0])
            {
                Log.e("Loading data......","000");
            }
            else
            {
                if (i == 0)
                {
                    temp = Distance(DBData[i], sampleData) + Distance(DBData[i+1], sampleData);
                }
                else {
                    temp = (Distance(DBData[i - 1], sampleData) + Distance(DBData[i], sampleData));
                }
                if (result2 > temp)
                {
                    result2 = temp;
                    label2 = i;
                }
            }
        }
        return label2;
    }

    private float Distance(float[] MagData, float[] sampleData)
    {                      // 实时采样点数据
        float distance;
        distance = (float) Math.sqrt(
                (MagData[0] - sampleData[0]) * (MagData[0] - sampleData[0]) +
                        (MagData[1] - sampleData[1]) * (MagData[1] - sampleData[1]) +
                        (MagData[2] - sampleData[2]) * (MagData[2] - sampleData[2])
        );
        return distance;
    }
}













