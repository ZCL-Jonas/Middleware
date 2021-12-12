package com.jonas.middleware.monitor.sms;

import android.content.Context;

import com.jonas.middleware.monitor.AbsMonitor;
import com.jonas.middleware.monitor.ISynergyServer;

public class SMSSyncMonitor implements AbsMonitor {

    private Context mContext;
    private ISynergyServer mISynergyServer;

    public SMSSyncMonitor(ISynergyServer monitor, Context context) {
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
