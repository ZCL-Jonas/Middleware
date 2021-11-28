package com.jonas.middleware.threadutil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jonas.middleware.utils.MessageConstant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTReadWriteThread extends Thread {
    private static final String TAG = "BleSocketRWThread";
    private BluetoothSocket bleSocket;
    private final OutputStream mOutputStream;
    private final InputStream mInputStream;
    private final Handler mHandler;

    public BTReadWriteThread(BluetoothSocket bleSocket, Handler mHandler) {
        this.bleSocket = bleSocket;
        this.mHandler = mHandler;

        OutputStream output = null;
        InputStream input = null;
        try {
            output = bleSocket.getOutputStream();
            input = bleSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "error : socket stream error");
        }

        mOutputStream = output;
        mInputStream = input;
    }

    @Override
    public void run() {
        super.run();
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                int bufferSize = mInputStream.read(buffer);
                if (bufferSize > 0) {
                    String data = new String(buffer, 0, bufferSize, "utf-8");
                    Message message = mHandler.obtainMessage(MessageConstant.MSG_READ_DATA, data);
                    mHandler.sendMessage(message);
                }
                Log.e(TAG, "message size :" + bufferSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (bleSocket != null) {
                bleSocket.close();
                bleSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写数据
     * @param data byte[]数据
     */
    public void write(byte[] data) {
        try {
            if (mOutputStream != null) {
                mOutputStream.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
