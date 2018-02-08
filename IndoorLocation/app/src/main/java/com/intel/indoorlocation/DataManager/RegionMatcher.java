package com.intel.indoorlocation.DataManager;


/**
 * Created by lpq on 17-6-21.
 * Based on Hausdorff Distance and K-nearest alg to find the most suitable points region.
 *
 */
public class RegionMatcher {

    public RegionMatcher()
    {
    }

    public float calHausdorffDis(float[] X, float[] Y)
    {
        float[] dis = new float[2];
        float minTemp;
        float sum;

        // X --> Y
        for(int i = 0; i < X.length; i++) {
            minTemp = 1000.f;
            for(int j = 0; j < Y.length; j++) {
                sum = Math.abs(X[i] - Y[j]);
                if(sum < minTemp)
                    minTemp = sum;
            }
            dis[0] += minTemp;
        }

        // Y --> X
        for (int j = 0; j < Y.length; j++)
        {
            minTemp = 1000.f;
            for (int i = 0; i < X.length; i++)
            {
                sum = Math.abs(Y[j] - X[i]);
                if (sum < minTemp)
                    minTemp = sum;
            }
            dis[1] += minTemp;
        }

        if (dis[0] > dis[1])
            return dis[0];
        return dis[1];
    }


    public int Matcher(float[][] DB, float[] sampleData){
        int position = 100000;                                 // 此处应该是无穷大的整形值
        float hausdorffDis = 1000000.0f;                         // 此处应该是无穷大的浮点数
        for (int i = 0; i < DB.length; i++)                    // DB.length获取二维数组的行数
        {
            float temp = calHausdorffDis(DB[i], sampleData);
            if (0 == sampleData[0])
            {
                //Log.d("Loading data......", "000");
            }
            else {
                if (temp < hausdorffDis) {
                    hausdorffDis = temp;
                    position = i;                                  // position用于记录最小距离的索引值----即当前定位的位置
                }
            }
        }
        return position;
    }

    public float calHausdorffDis2(float[][] X, float[] Y)
    {
        float[] dis = new float[2];
        float minTemp;
        float sum;
        // X --> Y
        for (int i = 0; i < X.length; i++)
        {
            minTemp = 1000.0f;
            for (int j = 0; j < Y.length; j++)
            {
                sum = Math.abs(X[i][j] - Y[j]);
                if (sum < minTemp)
                    minTemp = sum;
            }
            dis[0] += minTemp;
        }

        // Y --> X
        for (int i = 0; i < Y.length; i++)
        {
            minTemp = 1000.0f;
            for (int j = 0; j < X.length; j++)
            {
                sum = Math.abs(Y[i] - X[i][j]);
                if (sum < minTemp)
                    minTemp = sum;
            }
            dis[0] += minTemp;
        }

        if (dis[0] > dis[1])
            return dis[0];
        return dis[1];
    }
}
