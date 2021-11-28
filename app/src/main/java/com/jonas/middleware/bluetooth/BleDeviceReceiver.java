package com.jonas.middleware.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BleDeviceReceiver extends BroadcastReceiver {
    private static final String TAG = "BleDeviceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "action: " + action);
        if (action == null) {
            return;
        }

        switch (action) {
            // 蓝牙连接状态改变
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED :

                break;
            // 搜索结束,发现设备结束
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED :

                break;
            // 开始搜索
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED :

                break;
            // 本地蓝牙名称变化
            case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED :

                break;
            // Activity 请求发现设备
            case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE :

                break;
             // Activity 请求打开蓝牙
            case BluetoothAdapter.ACTION_REQUEST_ENABLE :

                break;
             // 扫描模式改变
            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED :

                break;
            // Broadcast 蓝牙状态改变
            case BluetoothAdapter.ACTION_STATE_CHANGED :

                break;
            // 远端设备建立低级连接
            case BluetoothDevice.ACTION_ACL_CONNECTED :

                break;
            // 低级断开请求将要断开连接
            case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED :

                break;
            // 低级断开请求断开连接
            case BluetoothDevice.ACTION_ACL_DISCONNECTED :

                break;
            // 蓝牙设备绑定状态
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED :

                break;
            // 远程设备的蓝牙类发送变化
            case BluetoothDevice.ACTION_CLASS_CHANGED :

                break;
            // 搜索到设备
            case BluetoothDevice.ACTION_FOUND :
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                break;
            case BluetoothDevice.ACTION_NAME_CHANGED :

                break;
             // 蓝牙配对请求
            case BluetoothDevice.ACTION_PAIRING_REQUEST :

                break;
             // UUID
            case BluetoothDevice.ACTION_UUID :

                break;
        }
        
    }
}
