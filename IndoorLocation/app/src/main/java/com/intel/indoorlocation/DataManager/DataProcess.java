package com.intel.indoorlocation.DataManager;

/**
 *
 * Created by LiPengqiang on 17-3-9.
 */
public class DataProcess {
    // 加权平均:平滑和均衡传感器数据，减小偶然数据突变的影响；
    // （新值） = （旧值）*（1 - a） + X * a , 其中a为权值,一般比较小，如0.1
    public static float[] WeightedSmooth (float[] data, float alpha)
    {
        float[] newData = new float[3];
        newData[0] = newData[0] * (1.0f - alpha) + data[0] * alpha;
        newData[1] = newData[1] * (1.0f - alpha) + data[1] * alpha;
        newData[2] = newData[2] * (1.0f - alpha) + data[2] * alpha;

        return newData;
    }

    // 抽取突变：去除静态和缓慢变化的数据背景，强调瞬间变化；
    // 采用上面加权平滑的逆算法。 alpha一般比较大，如0.8
    public static float[] ExtractMutations (float[] data, float aplha)
    {
        float[] newData = new float[3];

        return newData;
    }

    // 简单移动平均线：保留数据流最近的K个数据，取平均值
    // 保留传感器数据流中最近的K个数据，返回它们的平均值。k表示平均“窗口”的大小
    public static float[] SimMovAve (float[] data, int k)
    {
        float[] newData = new float[3];

        return newData;
    }
}
