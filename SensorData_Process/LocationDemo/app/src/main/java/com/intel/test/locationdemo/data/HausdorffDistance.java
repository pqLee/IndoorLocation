package com.intel.test.locationdemo.data;

/**
 * Created by lpq on 17-6-21.
 *
 */
public class HausdorffDistance {

    public HausdorffDistance()
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


    public int RegionMatcher(float[][] DB, float[] sampleData){
        int position = 100000;
        float hausdorffDis = 10000.0f;
        for (int i = 0; i < DB.length; i++)
        {
            float temp = calHausdorffDis(DB[i], sampleData);
            if (0 == sampleData[0])
            {
                //Log.e("Loading data......", "000");
            }
            else {
                if (temp < hausdorffDis) {
                    hausdorffDis = temp;
                    position = i;
                }
            }
        }

        //Log.d("Hausdorff Distance : ", String.valueOf(hausdorffDis));
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
