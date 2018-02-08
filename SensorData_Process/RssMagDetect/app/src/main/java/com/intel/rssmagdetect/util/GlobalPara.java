package com.intel.rssmagdetect.util;

/**
 * Created by lpq on 17-2-22.
 */
public class GlobalPara {
    public long timeOfStartScan=0;
    public long timeSinceStart=0;
    public int position_index=1;

    private volatile static GlobalPara globalPara = null;
    public static GlobalPara getInstance() {
        if (globalPara == null) {
            synchronized (GlobalPara.class) {
                if (globalPara == null) {
                    globalPara = new GlobalPara();
                }
            }
        }
        return globalPara;
    }
}
