package com.intel.rssmagdetect.file;

import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.intel.rssmagdetect.model.SensorsDataManager;
import com.intel.rssmagdetect.model.WifiDataManager;
import com.intel.rssmagdetect.util.GlobalPara;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by lpq on 17-2-22.
 * @description: 采集好的数据的存储.
 */

public class FileManager {
    /**
     * 这个函数每次存两个文件，"dataRssi_at_1" 和 "dataBssid.txt"
     * dataRssi_at_1存的是rssi和传感器数据，每个时刻的一组数据包括n个AP的rssi和15个传感器的数值，依次添加进去。
     * dataBssid存的是Wifi热点一些信息，顺序和上面的对应 注意：如果已存在该文件，这个函数创建的新的文件会覆盖之前的。
     * （APP第一次开启获取的BSSID顺序和关闭APP再开启进行采集得到的BSSID顺序是不一样的）
     * 但是app的逻辑是只有改变位置后，存储在内存的数据才清零，所以同一位置的多次存储并无影响。
     */
    public void saveData()
    {
        saveRssiAndSensors();   // Save the data.
        saveWifiBssids();       // Save the Wifi bssid.
    }

    private void saveRssiAndSensors()
    {
        try {
            String state = Environment.getExternalStorageState();
            if (!TextUtils.equals(state, Environment.MEDIA_MOUNTED)) {
                return;
            }
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard, "/CIPS-DataCollect/");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(sdCard, "dataRssi_at_"
                    + GlobalPara.getInstance().position_index + ".txt");
            FileOutputStream fOut = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fOut);
            for (int i = 0; i < WifiDataManager.getInstance().dataCount; i++) {
                // 存wifi的Rssi数据
                for (int j = 0; j < WifiDataManager.getInstance().dataBssid
                        .size(); j++) {
                    if (WifiDataManager.getInstance().dataRssi.get(j)
                            .containsKey(i)) {
                        dos.write((WifiDataManager.getInstance().dataRssi
                                .get(j).get(i) + "\t").getBytes());
                    } else {
                        dos.write((0 + "\t").getBytes()); // 没有的话就存0
                    }
                }
                // 存传感器数据，rss后面增加15个int
                SensorsDataManager sdm = SensorsDataManager.getInstance();
                String outString = sdm.dataMagnetic.get(0).get(i) + "\t"
                        + sdm.dataMagnetic.get(1).get(i) + "\t"
                        + sdm.dataMagnetic.get(2).get(i) + "\t"
                        + sdm.dataOrientation.get(0).get(i) + "\t"
                        + sdm.dataOrientation.get(1).get(i) + "\t"
                        + sdm.dataOrientation.get(2).get(i) + "\t"
                        + sdm.dataAccelerate.get(0).get(i) + "\t"
                        + sdm.dataAccelerate.get(1).get(i) + "\t"
                        + sdm.dataAccelerate.get(2).get(i) + "\t"
                        + sdm.dataGyroscope.get(0).get(i) + "\t"
                        + sdm.dataGyroscope.get(1).get(i) + "\t"
                        + sdm.dataGyroscope.get(2).get(i) + "\t"
                        + sdm.dataGravity.get(0).get(i) + "\t"
                        + sdm.dataGravity.get(1).get(i) + "\t"
                        + sdm.dataGravity.get(2).get(i) + "\n";
                System.out.println(outString);
                dos.write(outString.getBytes());
            }
            dos.close();

            Toast toast = Toast.makeText(
                    WifiDataManager.getInstance().activity,
                    "存储至“/CIPS-DataCollect”", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (FileNotFoundException e) {
            Toast.makeText(WifiDataManager.getInstance().activity, "存储失败。",
                    Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            Toast.makeText(WifiDataManager.getInstance().activity, "存储失败。",
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void saveWifiBssids()
    {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath()
                    + "/CIPS-DataCollect");
            directory.mkdirs();
            File file = new File(directory, "dataBssid.txt");
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStream fos = fOut;
            DataOutputStream dos = new DataOutputStream(fos);

            String[] tmpOutString = new String[WifiDataManager.getInstance().dataBssid
                    .size()];
            for (String bssid : WifiDataManager.getInstance().dataBssid
                    .keySet()) {
                int j = WifiDataManager.getInstance().dataBssid.get(bssid);
                String jString = j + 1 + "\tBSSID:\t" + bssid + "\tSSID:\t"
                        + WifiDataManager.getInstance().dataWifiNames.get(j)
                        + "\n";
                tmpOutString[j] = jString;
            }
            for (int i = 0; i < tmpOutString.length; i++) {
                dos.write(tmpOutString[i].getBytes());
            }
            dos.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }
}
