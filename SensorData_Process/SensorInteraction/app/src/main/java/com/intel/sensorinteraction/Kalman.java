package com.intel.sensorinteraction;

/**
 * Created by lpq on 17-8-7.
 *
 */
public class Kalman {

    float Angle = 0;        // Kalman_Filter的输出-----最优估计的角度
    float Gyro_x = 0;       // Kalman_Filter的输出-----最优估计的角速度
    float Q_angle = 0.010f; // 陀螺仪噪声的协方差
    float Q_gyro = 0.003f;  // 陀螺仪漂移的协方差
    float R_angle = 0.5f;   // 加速度计测量出角度的噪声
    float dt = 0.005f;      // 采样频率
    char C_0 = 1;
    float Q_bias = 0;       // 陀螺仪漂移
    float Angle_err = 0;
    float PCt_0 = 0;
    float PCt_1 = 0;
    float E = 0;
    float K_0 = 0;          // K是Kalman增益
    float K_1 = 0;
    float t_0 = 0;
    float t_1 = 0;
    float[] P_dot;          // 计算矩阵P的中间变量
    float[][] PP;           // 公式中P矩阵,X的协方差

    public Kalman()
    {
        P_dot = new float[]{0, 0, 0, 0};
        PP = new float[][]{{1, 0}, {0, 1}};
    }

    /**
     *
     * @param Gyro The gyroscope measurements.
     * @param Accele The angle calculate by accelerometer.
     * @return The Optimal angle value.
     */
    public float Kalman_Filter(float Gyro, float Accele)
    {
        /****************** Step 1 **********************/
        /**
         * Angle measurement model equation:
         * Angle_Estimated_Angle = Last_Best_Angle + (Angle_Velocity - Last_Optimal_Zero_Drift) * dt
         */
        Angle += (Gyro - Q_bias) * dt;

        /****************** Step 2 **********************/
        /**
         * The Kalman filter's aim is ----- Make P matrix minimum(here is the PP matrix)
         */
        P_dot[0] = Q_angle - PP[0][1] - PP[1][0];
        P_dot[1] = -PP[1][1];
        P_dot[2] = -PP[1][1];
        P_dot[3] = Q_gyro;
        PP[0][0] += P_dot[0] * dt;
        PP[0][1] += P_dot[1] * dt;
        PP[1][0] += P_dot[2] * dt;
        PP[1][1] += P_dot[3] * dt;

        /****************** Step 3 **********************/
        /**
         * Calculate the Kalman gain ----- Kg
         * Kg is a two-dimensional vector ----- Angle's gain and Q_bias's gain.
         */
        PCt_0 = C_0 * PP[0][0];
        PCt_1 = C_0 * PP[1][0];
        E = R_angle + C_0 * PCt_0;
        K_0 = PCt_0 / E;           // two kalman gains ----- Angle , Q_bias
        K_1 = PCt_1 / E;

        /****************** Step 4 **********************/
        Angle_err = Accele - Angle;
        Angle += K_0 * Angle_err;
        Q_bias += K_1 * Angle_err;
        Gyro_x = Gyro - Q_bias;    // The Optimal Angle Velocity value.

        /****************** Step 5 **********************/
        t_0 = PCt_0;
        t_1 = C_0 * PP[0][1];
        PP[0][0] -= K_0 * t_0;
        PP[0][1] -= K_0 * t_1;
        PP[1][0] -= K_1 * t_0;
        PP[1][1] -= K_1 * t_1;

        return Angle;
    }
}

