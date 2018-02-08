package com.intel.test.locationdemo.data;

/**
 * A 3*3 matrix process class.
 * Created by LiPengqiang on 17-3-9.
 */
public class MatrixManager {

    public static float[][] getDY(float[][] data, int h, int v)
    {
        float[][] newData = new float[3-1][3-1];

        for (int i = 0; i < newData.length; i++)
        {
            if (i < h - 1)
            {
                for (int j = 0; j < newData[i].length; j++)
                {
                    if (j < v - 1)

                    {
                        newData[i][j] = data[i][j];
                    }
                    else {
                        newData[i][j] = data[i][j + 1];
                    }
                }
            }
            else
            {
                for (int j = 0; j < newData[i].length; j++) {
                    if (j < v - 1) {
                        newData[i][j] = data[i + 1][j];
                    } else {
                        newData[i][j] = data[i + 1][j + 1];
                    }
                }
            }
        }
        return newData;
    }

    public static float getHLValue2(float[][] data)
    {
        float num1 = data[0][0] * data[1][1];
        float num2 = -data[0][1] * data[1][0];
        return num1 + num2;
    }

    public static float getHLValue3(float[][] data)
    {
        float num1 = data[0][0] * getHLValue2(getDY(data, 1, 1));
        float num2 = -data[0][1] * getHLValue2(getDY(data, 1, 2));
        float num3 = data[0][2] * getHLValue2(getDY(data, 1, 3));
        return num1 + num2 + num3;
    }

    public static float[][] getNi3(float[][] data)
    {
        float A = getHLValue3(data);
        float[][] newData = new float[3][3];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                float num;
                if ((i + j) % 2 == 0)
                {
                    num = getHLValue2(getDY(data, i+1, j+1));
                }
                else
                {
                    num = -getHLValue2(getDY(data, i+1, j+1));
                }
                newData[i][j] = num / A;
            }
        }
        newData = getA_T(newData);
        return newData;
    }

    public static float[][] getA_T(float[][] A)
    {
        float[][] A_T = new float[3][3];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                A_T[j][i] = A[i][j];
            }
        }
        return A_T;
    }

    public static float[][] getMultiply(float[][] A, float[][] B)
    {
        float[][] C = new float[3][3];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                float sum = 0;
                for (int k = 0; k < 3; k++)
                {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }

    public static float[] getMultiVector(float[][] matrix, float[] vector)
    {
        float[] result = new float[3];
        for (int i = 0; i < 3; i++)
        {
            float sum = 0;
            for (int j = 0; j < 3; j++)
            {
                sum += matrix[i][j] * vector[j];
            }
            result[i] = sum;
        }
        return result;
    }
}
