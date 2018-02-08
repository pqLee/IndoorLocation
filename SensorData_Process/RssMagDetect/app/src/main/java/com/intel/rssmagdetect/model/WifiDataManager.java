package com.intel.rssmagdetect.model;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.intel.rssmagdetect.ui.MainActivity;
import com.intel.rssmagdetect.file.FileManager;
import com.intel.rssmagdetect.ui.WifiListAdapter;
import com.intel.rssmagdetect.util.GlobalPara;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 1056550980@qq.com
 * Created by lpq on 17-2-22.
 * @description
 */
public class WifiDataManager {
    private WifiManager wifiManager;
    public List<ScanResult> scanResults = null;
    private volatile static WifiDataManager wiFiDataManager = null;
    public MainActivity activity;

    public ArrayList<HashMap<Integer, Integer>> dataRssi = new ArrayList<HashMap<Integer, Integer>>(); // 每行代表一个Wifi热点，对应一个map，map的第一个值是数据的index，第二个值是rssi
    public HashMap<String, Integer> dataBssid = new HashMap<String, Integer>();
    public ArrayList<String> dataWifiNames = new ArrayList<String>();
    public int dataCount = 0;

    public static WifiDataManager getInstance() {
        if (wiFiDataManager == null) {
            synchronized (WifiDataManager.class) {
                if (wiFiDataManager == null) {
                    wiFiDataManager = new WifiDataManager();
                }
            }
        }
        return wiFiDataManager;
    }

    public void init(MainActivity activity) {
        this.activity = activity;
        if (wifiManager == null) {
            wifiManager = (WifiManager) activity
                    .getSystemService(Context.WIFI_SERVICE);
        }
        if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Toast.makeText(activity, "正在开启wifi，请稍后...", Toast.LENGTH_SHORT)
                    .show();
            if (wifiManager == null) {
                wifiManager = (WifiManager) activity
                        .getSystemService(Context.WIFI_SERVICE);
            }
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }
    }

    public void startCollecting(MainActivity activity) {
        wifiManager.startScan();
        GlobalPara.getInstance().timeOfStartScan = GlobalPara.getInstance().timeSinceStart;
        activity.registerReceiver(cycleWifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private final BroadcastReceiver cycleWifiReceiver = new BroadcastReceiver() {
        @SuppressLint("UseSparseArrays")
        @Override
        public void onReceive(Context context, Intent intent) {
            scanResults = wifiManager.getScanResults();
            if (scanResults != null) {
                WifiListAdapter adapter = new WifiListAdapter(activity,
                        scanResults);
                activity.listView.setAdapter(adapter);
            }
            // 更新热点列表，只增不减，顺序不变，同时将RSSI记录下来
            for (int i = 0; i < scanResults.size(); i++) {
                if (!dataBssid.containsKey(scanResults.get(i).BSSID)) { // 新增一个wifi热点
                    dataBssid.put(scanResults.get(i).BSSID, dataBssid.size());
                    dataWifiNames.add(scanResults.get(i).SSID);
                    HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
                    tmp.put(dataCount, scanResults.get(i).level);
                    dataRssi.add(tmp);
                } else { // wifi热点已存在
                    dataRssi.get(dataBssid.get(scanResults.get(i).BSSID)).put(
                            dataCount, scanResults.get(i).level);
                }
            }
            dataCount++;

            while (GlobalPara.getInstance().timeSinceStart
                    - GlobalPara.getInstance().timeOfStartScan < 50) {
                // 等待，可以用来控制时间，，1 * 10ms, 正常的手机wifi扫描一次大约得一秒了
            }
            GlobalPara.getInstance().timeOfStartScan = GlobalPara.getInstance().timeSinceStart;

            // 收到后开始下一次扫描，控制一下时间，每秒最多两次
            wifiManager.startScan();
            activity.toggleButton.setText("关闭RSS数据采集" + "("
                    + String.valueOf(dataCount) + ")");
        }
    };

    public void endCollecting(MainActivity activity) {
        activity.unregisterReceiver(cycleWifiReceiver); // 取消监听
        SensorsDataManager.getInstance().updateSensorsData(); // 保持传感器和wifi数据的个数同步
        // 然后存储数据到文件
        new FileManager().saveData();

    }
}
