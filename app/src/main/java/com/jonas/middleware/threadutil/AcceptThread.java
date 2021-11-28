package com.jonas.middleware.threadutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.jonas.middleware.utils.MessageConstant;

import java.io.IOException;

public class AcceptThread extends Thread {
    private static final String TAG = "AcceptThread";
    private String name = "BluetoothAccept";
    private BluetoothServerSocket mBluetoothServerSocket;
    private BleSocketRWThread mBleSocketRWThread;
    private final Handler mHandler;

    public AcceptThread(BluetoothAdapter adapter, Handler handler) {
        mHandler = handler;
        try {
            mBluetoothServerSocket = adapter.listenUsingRfcommWithServiceRecord(name, MessageConstant.DEVICE_CONNECTION_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        if (mBluetoothServerSocket == null) {
            Log.e(TAG, "mBluetoothServerSocket == null");
            return;
        }
        BluetoothSocket socket = null;
        while (true) {
            mHandler.sendEmptyMessage(MessageConstant.MSG_START_LISTENING);
            try {
                socket = mBluetoothServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(MessageConstant.MSG_ERROR);
                release();
                break;
            }
            if (socket != null) {
                manageConnectSocket(socket);
            } else {
                Log.e(TAG, "accept socket is null");
            }
        }
    }

    private void manageConnectSocket(BluetoothSocket socket) {
        // 只支持同时处理一个连接
        // mConnectedThread不为空,踢掉之前的客户端
        if(mBleSocketRWThread != null) {
            mBleSocketRWThread.release();
        }

        // 主线程更新UI,连接到了一个客户端
        mHandler.sendEmptyMessage(MessageConstant.MSG_GOT_A_CLIENT);
        // 新建一个线程,处理客户端发来的数据
        mBleSocketRWThread = new BleSocketRWThread(socket, mHandler);
        mBleSocketRWThread.start();
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (mBluetoothServerSocket != null) {
                mBluetoothServerSocket.close();
                mBluetoothServerSocket = null;
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
