package com.intel.rssmagdetect.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.intel.rssmagdetect.R;
import com.intel.rssmagdetect.model.SensorsDataManager;
import com.intel.rssmagdetect.model.WifiDataManager;
import com.intel.rssmagdetect.util.GlobalPara;


public class MainActivity extends Activity {

    private Button previousPositionButton;
    private Button nextPositionButton;
    private TextView positionTextView;
    public static TextView dataTextView;
    public ToggleButton toggleButton;
    public ListView listView = null;

    private boolean startCollect = false;
    private Thread timeThread;
    private boolean cancelThread = false;

    private SensorsDataManager sensorsDataManager;
    private WifiDataManager wiFiDataManager;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        nextPositionButton = (Button) findViewById(R.id.nextPositionButton);
        previousPositionButton = (Button) findViewById(R.id.previousPositionButton);
        positionTextView = (TextView) findViewById(R.id.positionTextView);
        dataTextView = (TextView) findViewById(R.id.dataTextView);
        listView = (ListView) findViewById(R.id.listView1);
        nextPositionButton.setOnClickListener(changePositionClickListener);
        previousPositionButton.setOnClickListener(changePositionClickListener);

        sensorsDataManager = SensorsDataManager.getInstance();
        wiFiDataManager = wiFiDataManager.getInstance();
        sensorsDataManager.init(this);
        toggleButton.setOnCheckedChangeListener(new NewOnCheckedChangeListener());
        listView.setOnItemClickListener(wifiClickListener);
        timeThread = new Thread(new TimeCountRunnable());
        timeThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private AdapterView.OnItemClickListener wifiClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            ScanResult scanResult = wiFiDataManager.scanResults.get(arg2);
            AlertDialog.Builder wifiBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            wifiBuilder.setTitle(scanResult.SSID);
            ViewGroup connectWiFiGroup = (ViewGroup) MainActivity.this
                    .getLayoutInflater().inflate(R.layout.connect_wifi, null);
            TextView msgTextView = (TextView) connectWiFiGroup.getChildAt(0);
            msgTextView.setText("  SSID:\n  " + scanResult.SSID + "\n\n"
                    + "  BSSID:\n  " + scanResult.BSSID + "\n\n"
                    + "  frequency:\n  " + scanResult.frequency + " MHz\n\n"
                    + "  capabilities:\n  " + scanResult.capabilities + "\n\n"
                    + "  level:\n  " + scanResult.level + " dBm\n\n"
                    + "  describeContents:\n  " + scanResult.describeContents()
                    + "\n\n");
            wifiBuilder.setView(connectWiFiGroup);
            wifiBuilder.setPositiveButton("不知道密码",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast toast = Toast.makeText(MainActivity.this,
                                    "我也不知道-.-", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });
            wifiBuilder.setNegativeButton("连接",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast toast = Toast.makeText(MainActivity.this,
                                    "请自行前往系统页面连接,谢谢！", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });

            wifiBuilder.setNeutralButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "-.-",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            AlertDialog alertDialog = wifiBuilder.create();
            alertDialog.show();
        }
    };

    // 两次返回退出
    private long exitTime = 0;

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if ((System.currentTimeMillis() - exitTime) > 2000)
            {
                Toast.makeText(getApplicationContext(), "再次点击“返回”退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else
            {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop()
    {
        sensorsDataManager.unregist();
        cancelThread = true;
        super.onStop();
    }


    private class TimeCountRunnable implements Runnable
    {
        @Override
        public void run() {
            try {
                while (!cancelThread) {
                    Thread.sleep(10);// 睡10ms
                    GlobalPara.getInstance().timeSinceStart++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class NewOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            startCollect = isChecked;
            wiFiDataManager.init(MainActivity.this);
            if (startCollect)
                wiFiDataManager.startCollecting(MainActivity.this);
            else
                wiFiDataManager.endCollecting(MainActivity.this);
        }
    }

    private View.OnClickListener changePositionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (toggleButton.isChecked()) {
                Toast.makeText(MainActivity.this, "请先关闭数据采集再改变位置。",
                        Toast.LENGTH_SHORT).show();
            } else {
                wiFiDataManager.dataCount = 0;// 改变位置了,要在新的位置上重新采集，这个置零
                sensorsDataManager.dataClear();
                // 对每个wifi热点的map清空，并不是删除，wifi热点的个数只增不减，但每个热点的数据会被清空
                for (int i = 0; i < wiFiDataManager.dataRssi.size(); i++) {
                    wiFiDataManager.dataRssi.get(i).clear();
                }
                if (v.getId() == nextPositionButton.getId()) {
                    GlobalPara.getInstance().position_index++;
                    positionTextView.setText("当前位置："
                            + GlobalPara.getInstance().position_index);
                } else if (v.getId() == previousPositionButton.getId()) {
                    GlobalPara.getInstance().position_index--;
                    positionTextView.setText("当前位置："
                            + GlobalPara.getInstance().position_index);
                }
            }
        }
    };
}
