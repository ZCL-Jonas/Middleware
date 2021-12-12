package com.jonas.middleware.alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jonas.middleware.R;
import com.jonas.middleware.service.NotificationCollectorMonitorService;

import java.util.Set;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AlarmActivity";
    // A public action send by AlarmService when the alarm has started.
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    // A public action sent by AlarmService when the alarm has stopped for any reason.
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";
    // AlarmActivity listens for this broadcast intent, so that other applications
    // can snooze the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    // AlarmActivity listens for this broadcast intent, so that other applications
    // can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        initView();
        initBroadcast();
    }

    private void initView() {
        findViewById(R.id.bt_notification).setOnClickListener(this);
    }

    //检测通知监听服务是否被授权
    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        return packageNames.contains(context.getPackageName());
    }

    //打开通知监听设置页面
    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //把应用的NotificationListenerService实现类disable再enable，即可触发系统rebind操作
    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(this, NotificationCollectorMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(this, NotificationCollectorMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           Log.e(TAG, "onServiceConnected : " + name.getPackageName() + " " + name.getClassName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected : " + name.getClassName());
        }
    };

    private void initBroadcast() {
        alarmReceiver = new AlarmReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ALARM_ALERT_ACTION);
        intentFilter.addAction(ALARM_DONE_ACTION);
        intentFilter.addAction(ALARM_SNOOZE_ACTION);
        intentFilter.addAction(ALARM_DISMISS_ACTION);
        registerReceiver(alarmReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_notification) {
            Toast.makeText(AlarmActivity.this, "开启监听通知权限", Toast.LENGTH_SHORT).show();
            // 判断是否开启监听通知权限
            if (isNotificationListenerEnabled(AlarmActivity.this)) {
                Intent serviceIntent = new Intent(AlarmActivity.this, NotificationCollectorMonitorService.class);
                startService(serviceIntent);
            } else {
                // 去开启 监听通知权限
                openNotificationListenSettings();
            }
        } else if (v.getId() == R.id.bt_send) {
            createNotification(this);
        }

    }

    private void createNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncBuilder = new NotificationCompat.Builder(context);
        ncBuilder.setContentTitle("My Notification");
        ncBuilder.setContentText("Notification Listener Service Example");
        ncBuilder.setTicker("Notification Listener Service Example");
        ncBuilder.setAutoCancel(true);
        manager.notify((int)System.currentTimeMillis(),ncBuilder.build());
    }

    private static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "action :" + action);
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case ALARM_ALERT_ACTION:
                        break;
                    case ALARM_DISMISS_ACTION:
                        break;
                    case ALARM_DONE_ACTION:
                        break;
                    case ALARM_SNOOZE_ACTION:
                        break;
                    default:
                        break;
                }
            }
        }
    }
}