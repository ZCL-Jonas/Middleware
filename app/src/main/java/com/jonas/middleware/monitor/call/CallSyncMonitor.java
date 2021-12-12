package com.jonas.middleware.monitor.call;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.jonas.middleware.monitor.AbsMonitor;
import com.jonas.middleware.monitor.ISynergyServer;

public class CallSyncMonitor implements AbsMonitor {

    private ISynergyServer mISynergyServer;
    private Context mContext;
    private TelephonyManager mTelephonyManager;

    public CallSyncMonitor(ISynergyServer monitor, Context context) {
        mISynergyServer = monitor;
        mContext = context;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
