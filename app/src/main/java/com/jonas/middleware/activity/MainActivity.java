package com.jonas.middleware.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jonas.middleware.R;
import com.jonas.middleware.adapter.DeviceAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "BluetoothReceiver";
    private static final int REQUEST_OPEN_BT_CODE = 1001;
    private BluetoothReceiver mBleReceiver;
    private RecyclerView rv;
    private RecyclerView rvPaired;
    private LinearLayout loadingLay;
    private TextView scanBle;
    private MyHandler myHandler;
    private DeviceAdapter mDeviceAdapter;
    private DeviceAdapter mPairDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private final List<BluetoothDevice> pairDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBleReceiver = new BluetoothReceiver();
        myHandler = new MyHandler();
        initView();
        initAdapter();
        initPairAdapter();
        permissionsRequest();
        //获取已配对的设备
        searchBoundDevice();
    }

    private void initView() {
        scanBle = findViewById(R.id.scan_devices);
        scanBle.setOnClickListener(this);
        loadingLay = findViewById(R.id.loading_lay);
        rv = findViewById(R.id.rv);
        rvPaired = findViewById(R.id.rv_paired);
    }

    private void initAdapter() {
        mDeviceAdapter = new DeviceAdapter(R.layout.bluetooch_item, deviceList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mDeviceAdapter);
        mDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            //点击时获取状态，如果已经配对过了就不需要在配对
            if (deviceList.get(position).getBondState() == BluetoothDevice.BOND_NONE) {
                createOrRemoveBond(1, deviceList.get(position));//开始匹配
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void initPairAdapter() {
        mPairDeviceAdapter = new DeviceAdapter(R.layout.bluetooch_item, pairDeviceList);
        rvPaired.setLayoutManager(new LinearLayoutManager(this));
        rvPaired.setAdapter(mPairDeviceAdapter);
        mPairDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            //点击时获取状态，如果已经配对过了就不需要在配对
            BluetoothDevice device = pairDeviceList.get(position);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("address", pairDeviceList.get(position).getAddress());
                startActivity(intent);
            } else if (device.getBondState() == BluetoothDevice.BOND_NONE){
                device.createBond();
            }
        });
        mPairDeviceAdapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            showDialog("确定要取消配对吗？", (dialog, which) -> {
                //取消配对
                createOrRemoveBond(2, pairDeviceList.get(position));//取消匹配
            });
            return false;
        });
    }

    private void permissionsRequest() {
        RxPermissions rxPermissions = new RxPermissions(this);
        Disposable disposable = rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {//申请成功
                        registerBluetooth();//初始化蓝牙配置
                    } else {//申请失败
                        showMsg("权限未开启");
                    }
                });
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void registerBluetooth() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(mBleReceiver, intentFilter);
    }

    /**
     * 获取已配对设备
     */
    private void searchBoundDevice() {
        if (mBluetoothAdapter == null) {
            showMsg("不支持蓝牙");
            return;
        }
        pairDeviceList.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //如果获取的结果大于0，则开始逐个解析
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //过滤掉设备名称为null的设备, 防止重复添加
                if (isValidDevice(device, pairDeviceList)) {
                    pairDeviceList.add(device);
                }
            }
        }
        mPairDeviceAdapter.changeBondDevice();
    }

    private boolean isValidDevice(BluetoothDevice device, List<BluetoothDevice> devices) {
        return !devices.contains(device) && device.getName() != null;
    }

    /**
     * 创建或者取消匹配
     *
     * @param type   处理类型 1 匹配  2  取消匹配
     * @param device 设备
     */
    private void createOrRemoveBond(int type, BluetoothDevice device) {
        Method method = null;
        try {
            switch (type) {
                case 1://开始匹配
                    method = BluetoothDevice.class.getMethod("createBond");
                    method.invoke(device);
                    break;
                case 2://取消匹配
                    method = BluetoothDevice.class.getMethod("removeBond");
                    method.invoke(device);
                    pairDeviceList.remove(device);//清除列表中已经取消了配对的设备
                    break;
                case 3://
                    method = BluetoothDevice.class.getMethod("cancelBond");
                    method.invoke(device);
                    break;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void addDevice(BluetoothDevice device, List<BluetoothDevice> devices, DeviceAdapter adapter) {
        if (device == null || devices == null) {
            return;
        }
        if (isValidDevice(device, devices)) {
            devices.add(device);
        }
        adapter.changeBondDevice();
    }

    private void removeDevice(BluetoothDevice device, List<BluetoothDevice> devices, DeviceAdapter adapter) {
        if (device == null || devices == null) {
            return;
        }
        //防止重复添加
        if (devices.contains(device)) {
            //过滤掉设备名称为null的设备
            devices.remove(device);
        }
        adapter.changeBondDevice();
    }

    private void bluetoothStateChange(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_NONE:
                removeDevice(device, pairDeviceList, mPairDeviceAdapter);
                //searchUnBoundDevice();
                mDeviceAdapter.changeBondDevice();
                showMsg("解除配对");
                Log.e(TAG, "解除配对");
                break;
            case BluetoothDevice.BOND_BONDED:
                addDevice(device, pairDeviceList, mPairDeviceAdapter);
                removeDevice(device, deviceList, mDeviceAdapter);
                showMsg("配对成功");
                Log.e(TAG, "配对成功");
                break;
            case BluetoothDevice.BOND_BONDING:
                showMsg("正在配对");
                Log.e(TAG, "正在配对");
                break;
        }
    }

    /**
     * 弹窗
     *
     * @param dialogTitle     标题
     * @param onClickListener 按钮的点击事件
     */
    private void showDialog(String dialogTitle, @NonNull DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogTitle);
        builder.setPositiveButton("确定", onClickListener);
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        if (view == scanBle) {
            searchUnBoundDevice();
        }
    }

    private void searchUnBoundDevice() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                if (mDeviceAdapter != null) {//当适配器不为空时，这时就说明已经有数据了，所以清除列表数据，再进行扫描
                    deviceList.clear();
                    mDeviceAdapter.changeBondDevice();
                }
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();
            } else {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_OPEN_BT_CODE);
            }
        } else {
            showMsg("不支持蓝牙");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_BT_CODE) {
            if (resultCode == RESULT_OK) {
                showMsg("蓝牙打开成功");
            } else {
                showMsg("蓝牙打开失败");
            }
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1000) {
                showMsg((String) msg.obj);
            }
        }
    }

    private void bluetoothOpenState(Intent intent) {
        int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        switch (status) {
            case BluetoothAdapter.STATE_OFF:
                showMsg("蓝牙已关闭");
                break;
            case BluetoothAdapter.STATE_ON:
                showMsg("蓝牙已打开");
                searchBoundDevice();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                showMsg("蓝牙关闭中...");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                showMsg("蓝牙打开中...");
                break;
            default:

                break;
        }
    }

    /**
     * 销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //卸载广播接收器
        unregisterReceiver(mBleReceiver);
    }

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String action = intent.getAction();
            Log.e(TAG, "action: " + action);
            switch (action) {
                // 蓝牙连接状态改变
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:

                    break;
                // 搜索结束,发现设备结束
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    loadingLay.setVisibility(View.GONE);//显示加载布局
                    mDeviceAdapter.changeBondDevice();
                    break;
                // 开始搜索
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    loadingLay.setVisibility(View.VISIBLE);//显示加载布局
                    break;
                // 本地蓝牙名称变化
                case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:

                    break;
                // Activity 请求发现设备
//                case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE :
//
//                    break;
//                // Activity 请求打开蓝牙
//                case BluetoothAdapter.ACTION_REQUEST_ENABLE :
//
//                    break;
                // 扫描模式改变
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:

                    break;
                // Broadcast 蓝牙状态改变
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    bluetoothOpenState(intent);
                    break;
                // 远端设备建立低级连接
                case BluetoothDevice.ACTION_ACL_CONNECTED:

                    break;
                // 低级断开请求将要断开连接
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:

                    break;
                // 低级断开请求断开连接
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:

                    break;
                // 蓝牙设备配对状态
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    bluetoothStateChange(device);
                    break;
                // 远程设备的蓝牙类发送变化
                case BluetoothDevice.ACTION_CLASS_CHANGED:

                    break;
                // 搜索到设备
                case BluetoothDevice.ACTION_FOUND:
                    if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                        addDevice(device, deviceList, mDeviceAdapter);
                    }
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:

                    break;
                // 蓝牙配对请求
                case BluetoothDevice.ACTION_PAIRING_REQUEST:

                    break;
                // UUID
                case BluetoothDevice.ACTION_UUID:
                    ParcelUuid[] uuids = device.getUuids();
                    for (ParcelUuid uuid : uuids) {
                        Log.e(TAG, "uuid : " + uuid);
                    }
                    ParcelUuid uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                    Log.e(TAG, "--- uuid : " + uuid);
                    break;
            }

        }
    }
}
