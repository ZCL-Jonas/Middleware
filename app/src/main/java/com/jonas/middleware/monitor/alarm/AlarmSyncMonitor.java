package com.jonas.middleware.monitor.alarm;

import android.content.Context;

import com.jonas.middleware.monitor.AbsMonitor;
import com.jonas.middleware.monitor.ISynergyServer;

public class AlarmSyncMonitor implements AbsMonitor {

    private ISynergyServer mISynergyServer;
    private Context mContext;

    public AlarmSyncMonitor(ISynergyServer monitor, Context context) {
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
