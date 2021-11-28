package com.jonas.middleware.threadutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jonas.middleware.utils.MessageConstant;

import java.io.IOException;

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BTReadWriteThread mBTReadWriteThread;
    private BluetoothSocket mBluetoothSocket;
    private final Handler mHandler;

    public ConnectThread(BluetoothAdapter adapter, BluetoothDevice device, Handler handler) {
        mBluetoothAdapter = adapter;
        mHandler = handler;
        mBluetoothDevice = device;
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
        Message message = mHandler.obtainMessage(MessageConstant.MSG_CONNECTED_TO_SERVER, mBluetoothDevice);
        mHandler.sendMessage(message);
        // 新建一个线程进行通讯,不然会发现线程堵塞
        mBTReadWriteThread = new BTReadWriteThread(mSocket, mHandler);
        mBTReadWriteThread.start();
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
            if (mBTReadWriteThread != null) {
                mBTReadWriteThread.release();
                mBTReadWriteThread.interrupt();
                mBTReadWriteThread = null;
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
        if (mBTReadWriteThread != null) {
            mBTReadWriteThread.write(data);
        }
    }
}
