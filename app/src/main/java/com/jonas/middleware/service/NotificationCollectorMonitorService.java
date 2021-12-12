package com.jonas.middleware.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

public class NotificationCollectorMonitorService extends NotificationListenerService {
    private static final String TAG = "MyNotificationListenerS";
    private MyBinder mBinder = new MyBinder();
    public  class MyBinder extends Binder {
        public NotificationCollectorMonitorService getService(){
            return NotificationCollectorMonitorService.this;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.e(TAG, "onNotificationPosted(sbn):" + sbn.toString());
        super.onNotificationPosted(sbn);
        showMsg(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        Log.e(TAG, "onNotificationRemoved(sbn, rankingMap):" + sbn.toString());
        super.onNotificationRemoved(sbn, rankingMap);
        showMsg(sbn);
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.e(TAG, "attachBaseContext(context)");
        super.attachBaseContext(base);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        Log.e(TAG, "onNotificationPosted(sbn, map)" + sbn.toString());
        super.onNotificationPosted(sbn, rankingMap);
        showMsg(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.e(TAG, "onNotificationRemoved(sbn)" + sbn.toString());
        super.onNotificationRemoved(sbn);
        showMsg(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        Log.e(TAG, "onNotificationRemoved(sbn, map, reason)" + sbn.toString());
        super.onNotificationRemoved(sbn, rankingMap, reason);
        showMsg(sbn);
    }

    @Override
    public void onListenerConnected() {
        Log.e(TAG, "onListenerConnected()");
        super.onListenerConnected();
    }

    @Override
    public void onListenerDisconnected() {
        Log.e(TAG, "onListenerDisconnected()");
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        Log.e(TAG, "onNotificationRankingUpdate(map)");
        super.onNotificationRankingUpdate(rankingMap);
    }

    @Override
    public void onListenerHintsChanged(int hints) {
        Log.e(TAG, "onListenerHintsChanged(hints)");
        super.onListenerHintsChanged(hints);
    }

    @Override
    public void onSilentStatusBarIconsVisibilityChanged(boolean hideSilentStatusIcons) {
        Log.e(TAG, "onListenerDisconnected(hideSilentStatusIcons)");
        super.onSilentStatusBarIconsVisibilityChanged(hideSilentStatusIcons);
    }

    @Override
    public void onNotificationChannelModified(String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
        Log.e(TAG, "onNotificationChannelModified(pkg, user, channel, type)");
        super.onNotificationChannelModified(pkg, user, channel, modificationType);
    }

    @Override
    public void onNotificationChannelGroupModified(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
        Log.e(TAG, "onNotificationChannelGroupModified(pkg, user, group, type)");
        super.onNotificationChannelGroupModified(pkg, user, group, modificationType);
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        Log.e(TAG, "onInterruptionFilterChanged(filter)");
        super.onInterruptionFilterChanged(interruptionFilter);
    }

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        Log.e(TAG, "getActiveNotifications()");
        return super.getActiveNotifications();
    }

    @Override
    public StatusBarNotification[] getActiveNotifications(String[] keys) {
        Log.e(TAG, "getActiveNotifications(keys)");
        return super.getActiveNotifications(keys);
    }

    @Override
    public RankingMap getCurrentRanking() {
        Log.e(TAG, "getCurrentRanking()");
        return super.getCurrentRanking();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "onRebind()");
        super.onRebind(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
    }

    private void showMsg(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;

        String packageName = sbn.getPackageName();

        if (extras != null) {
            //获取通知消息标题
            String title = extras.getString(Notification.EXTRA_TITLE);
            // 获取通知消息内容
            Object msgText = extras.getCharSequence(Notification.EXTRA_TEXT);

            //注意：获取的通知信息和短信的传递内容不一样 短信为SpannableString 这里容易造成转换异常
            if (msgText instanceof SpannableString) {
                Log.d(TAG, "is SpannableString ...." + ((SpannableString) msgText).subSequence(0, ((SpannableString) msgText).length()));

            } else {
                Log.d(TAG, "showMsg packageName=" + packageName + ",title=" + title + ",msgText=" + msgText);
            }

        } else {
            Log.d(TAG, "is null ...." + packageName);
        }

    }


    /**
     *
     * @param context
     * @param id 图标id
     * @param pkgName 图标所在的包名
     * @return bitmap
     */
    public static Bitmap getSmallIcon(Context context, String pkgName, int id) {
        Bitmap smallIcon = null;
        Context remotePkgContext;
        try {
            remotePkgContext = context.createPackageContext(pkgName, 0);
            Drawable drawable = remotePkgContext.getResources().getDrawable(id);
            if (drawable != null) {
                smallIcon = ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return smallIcon;
    }
}
