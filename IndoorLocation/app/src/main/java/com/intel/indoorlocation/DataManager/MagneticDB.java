package com.intel.indoorlocation.DataManager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Store the sampled magnetic_field data.
 * Created by lpq on 17-3-14.
 */
public class MagneticDB {
    private static final int MAX_NODE = 100;
    private static final int SAMPLE_NODE = 7;
    public static final float THRESHOLD = 4.0f;
    public float[][] DataBase = new float[MAX_NODE][3];
    private float[][] MagneticDB = new float[MAX_NODE][3];
    public float[][] LocationXY = new float[MAX_NODE][2];  // 每个标记点在屏幕地图的位置
    private int LocationNumber = 8;                            // 表示离得最近点的编号0~7, ８个位置,  当前的定位

    public Map<float[], float[]> data;

    public MagneticDB() {
        data = new HashMap<>();
        //InitLocationXY(LocationNumber);
        InitMagneticDB();
        InitMap();
    }

    public void RawDataProcessor (float[][] database)
    {

    }






    private float MinDistance(float[][] DataBase, float[] SampleData)                 // 求当前采样点到数据库中所有点的最小值
    {
        float[] result = new float[SAMPLE_NODE];      // 保留当前采样值与数据库中所有点的欧氏距离
        for (int i = 0; i < SAMPLE_NODE; i++) {
            result[i] = (float) Math.sqrt((DataBase[i][0] - SampleData[0]) * (DataBase[i][0] - SampleData[0]) +
                    (DataBase[i][1] - SampleData[1]) * (DataBase[i][1] - SampleData[1]) +
                    (DataBase[i][2] - SampleData[2]) * (DataBase[i][2] - SampleData[2]));
        }
        float distance = result[0];

        // 求最临近的那个点.
        for (int i = 0; i < SAMPLE_NODE; i++) {
            if (distance >= result[i]) {
                distance = result[i];
                LocationNumber = i;                     // 定位点信息
            }
        }
        return distance;   // 返回的是所有欧氏距离中的最小值.
    }

    public float[] LocationPos(float[] SampleData)     // 频繁调用
    {
        if (MinDistance(MagneticDB, SampleData) < THRESHOLD) {
            return LocationXY[LocationNumber];                      // 返回的是最近点的坐标
        } else
            return new float[]{0, 0};
    }

    // 保存 " 地磁信息 ---- 位置信息 " 这样的键值对，一一对应的.  每采集一个地磁数据，加入向对应的位置信息，形成一个键值对.
    public void PutDataToMap(float[] MagData, float[] PosData)
    {
        data.put(MagData, PosData);
    }

    private void InitMap() {
        for (int i = 0; i < SAMPLE_NODE; i++)
        {
            PutDataToMap(DataBase[i], LocationXY[i]);
        }
    }

    private void InitMagneticDB ()
    {
        /*** cube data ***/
/*        MagneticDB[0] = new float[]{-1.8f, 4.0f, 32.8f};        // P0
        MagneticDB[1] = new float[]{15.5f, 0.5f, 30.6f};        // P1
        MagneticDB[2] = new float[]{21.5f, 1.2f, 37.7f};       // P2
        MagneticDB[3] = new float[]{33.0f, -0.6f, 43.5f};       // P3
        MagneticDB[4] = new float[]{21.0f, 3.4f, 30.0f};       // P4
        MagneticDB[5] = new float[]{-4.0f, 2.6f, 37.0f};      // P5
        MagneticDB[6] = new float[]{6.2f, 0.9f, 15.0f};       // P6
        MagneticDB[7] = new float[]{13.5f, -0.92f, 10.2f};           // P7      */

        /*** 2F03 meeting room data ***/
        MagneticDB[0] = new float[]{16.4f, -3.8f, 36.8f};        // P0
        MagneticDB[1] = new float[]{36.7f, -1.7f, 42.6f};        // P1
        MagneticDB[2] = new float[]{20.0f, 1.3f, 44.0f};       // P2
        MagneticDB[3] = new float[]{30.0f, -0.2f, 13.7f};       // P3
    }
}








