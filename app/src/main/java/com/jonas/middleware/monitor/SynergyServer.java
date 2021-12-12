package com.jonas.middleware.monitor;

import android.content.Context;

public class SynergyServer implements ISynergyServer , AbsMonitor{
    private SynergySyncServer mSynergySyncServer;


    public SynergyServer(Context context) {
        mSynergySyncServer = new SynergySyncServer(this, context);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
