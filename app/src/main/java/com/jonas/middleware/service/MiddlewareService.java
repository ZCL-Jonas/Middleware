package com.jonas.middleware.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jonas.middleware.threadutil.AcceptThread;
import com.jonas.middleware.utils.ChatController;
import com.jonas.middleware.utils.DataProtocolUtil;
import com.jonas.middleware.utils.MessageConstant;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class MiddlewareService extends Service {

    public static final String ACTION_READ_MESSAGE = "ACTION_READ_MESSAGE";
    private static final String TAG = "MiddlewareService";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private AcceptThread mAcceptThread;

    //client 可以通过Binder获取Service实例
    public class MyBinder extends Binder {
        public MiddlewareService getService() {
            return MiddlewareService.this;
        }
    }

    //通过binder实现调用者client与Service之间的通信
    private final MyBinder binder = new MyBinder();

    private final Random generator = new Random();

    @Override
    public void onCreate() {
        Log.e(TAG, "MiddlewareService - onCreate - Thread = " + Thread.currentThread().getName());
        super.onCreate();
    }

    /**
     * 开始启动server socket
     *
     * @param mac mac
     */
    public void initBluetooth(String mac, Handler handler) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = getBoundDevice(mac).orElse(null);
        mAcceptThread = new AcceptThread(mBluetoothAdapter, handler);
        mAcceptThread.start();
    }

    private Optional<BluetoothDevice> getBoundDevice(String mac) {
        if (mBluetoothAdapter == null) {
            return Optional.empty();
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //如果获取的结果大于0，则开始逐个解析
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //过滤掉设备名称为null的设备, 防止重复添加
                if (TextUtils.equals(mac, device.getAddress())) {
                    return Optional.of(device);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "MiddlewareService - onStartCommand - startId = " + startId + ", Thread = " + Thread.currentThread().getName());
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "MiddlewareService - onBind - Thread = " + Thread.currentThread().getName());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "MiddlewareService - onUnbind - from = " + intent.getStringExtra("from"));
        return false;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "MiddlewareService - onDestroy - Thread = " + Thread.currentThread().getName());
        if (mAcceptThread != null) {
            mAcceptThread.release();
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        super.onDestroy();
    }

    /**
     * 发送消息
     *
     * @param msg 消息
     */
    public void sendMessage(String msg) {
        byte[] data = DataProtocolUtil.encodePackage(msg);
        if (mAcceptThread != null) {
            mAcceptThread.sendData(data);
        }
    }

    //getRandomNumber是Service暴露出去供client调用的公共方法
    public int getRandomNumber() {
        return generator.nextInt();
    }
}
