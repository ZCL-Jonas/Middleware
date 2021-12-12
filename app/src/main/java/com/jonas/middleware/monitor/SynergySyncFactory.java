package com.jonas.middleware.monitor;

import android.content.Context;

import com.jonas.middleware.monitor.alarm.AlarmSyncMonitor;
import com.jonas.middleware.monitor.assist.AssistSyncMonitor;
import com.jonas.middleware.monitor.call.CallSyncMonitor;
import com.jonas.middleware.monitor.camera.CameraSyncMonitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
class SynergySyncFactory {
    private static final Map<Byte, Class> monitorMap = new HashMap<>();

    static {
        monitorMap.put(CatIdConstant.ASSIST_ID, AssistSyncMonitor.class);
        monitorMap.put(CatIdConstant.CAMERA_ID, CameraSyncMonitor.class);
        monitorMap.put(CatIdConstant.ALARM_ID, AlarmSyncMonitor.class);
        monitorMap.put(CatIdConstant.CALL_ID, CallSyncMonitor.class);
    }

    public static AbsMonitor load(Byte catId, ISynergyServer monitor, Context context) {
        AbsMonitor absMonitor = null;
        try {
            Class<Object> mClass = monitorMap.get(catId);
            Constructor<Object> constructor = mClass.getConstructor(ISynergyServer.class, Context.class);
            Object object = constructor.newInstance(monitor, context);
            if (object instanceof AbsMonitor) {
                return (AbsMonitor)object;
            }
        } catch (IllegalAccessException | InstantiationException |
                NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return absMonitor;
    }

    public static Set<Byte> getCatKey() {
        return monitorMap.keySet();
    }
}


