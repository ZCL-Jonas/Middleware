package com.jonas.middleware.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.jonas.middleware.threadutil.AcceptThread;
import com.jonas.middleware.threadutil.ConnectThread;


public class ChatController {

    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;

    public void startChatWith(BluetoothAdapter adapter, BluetoothDevice device, Handler handler) {
        mConnectThread = new ConnectThread(adapter, device, handler);
        mConnectThread.start();
    }

    public void waitingFriends(BluetoothAdapter adapter, Handler handler) {
        mAcceptThread = new AcceptThread(adapter, handler);
        mAcceptThread.start();
    }

    public void writeMessage(String msg) {
        byte[] data = DataProtocolUtil.encodePackage(msg);
        if (mConnectThread != null) {
            mConnectThread.sendData(data);
        }
        if (mAcceptThread != null) {
            mAcceptThread.sendData(data);
        }
    }

    /**
     * 以下是单例写法
     */
    private static class ChatControlHolder{
        private static final ChatController mInstance = new ChatController();
    }

    public static ChatController getInstance(){
        return ChatControlHolder.mInstance;
    }
}
