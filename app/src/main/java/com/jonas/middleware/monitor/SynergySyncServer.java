package com.jonas.middleware.monitor;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SynergySyncServer {
    private final Map<Integer, AbsMonitor> absMonitorMap = new HashMap<>();

    public SynergySyncServer(ISynergyServer monitor, Context context) {
        Set<Byte> catKeys = SynergySyncFactory.getCatKey();
        for (Byte key: catKeys) {
            AbsMonitor absMonitor = SynergySyncFactory.load(key, monitor, context);
            absMonitorMap.put(key.intValue(), absMonitor);
        }
    }
}
