package com.jonas.middleware.monitor.camera;

import android.content.Context;

import com.jonas.middleware.monitor.AbsMonitor;
import com.jonas.middleware.monitor.ISynergyServer;


public class CameraSyncMonitor implements AbsMonitor {

    private Context mContext;
    private ISynergyServer mISynergyServer;

    public CameraSyncMonitor(ISynergyServer monitor, Context context) {
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
