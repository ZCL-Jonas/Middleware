package com.jonas.middleware.threadutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.jonas.middleware.utils.MessageConstant;

import java.io.IOException;

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private final BluetoothAdapter mBluetoothAdapter;
    private BleSocketRWThread mBleSocketRWThread;
    private BluetoothSocket mBluetoothSocket;
    private final Handler mHandler;

    public ConnectThread(BluetoothAdapter adapter, BluetoothDevice device, Handler handler) {
        mBluetoothAdapter = adapter;
        mHandler = handler;

        BluetoothSocket mBleSocket = null;

        try {
            mBleSocket = device.createRfcommSocketToServiceRecord(MessageConstant.DEVICE_CONNECTION_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBluetoothSocket = mBleSocket;
    }


    @Override
    public void run() {
        super.run();

        if (mBluetoothSocket == null) {
            Log.e(TAG, "mBluetoothSocket == null");
            return;
        }

        mBluetoothAdapter.cancelDiscovery();
        try {
            mBluetoothSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "connect error : " + e.getMessage());
            release();
            return;
        }

        manageConnectedSocket(mBluetoothSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mSocket) {
        // 通知主线程连接上了服务端socket，更新UI
        mHandler.sendEmptyMessage(MessageConstant.MSG_CONNECTED_TO_SERVER);
        // 新建一个线程进行通讯,不然会发现线程堵塞
        mBleSocketRWThread = new BleSocketRWThread(mSocket, mHandler);
        mBleSocketRWThread.start();
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     * @param data 数据
     */
    public void sendData (byte[] data) {
        if (mBleSocketRWThread != null) {
            mBleSocketRWThread.write(data);
        }
    }
}
