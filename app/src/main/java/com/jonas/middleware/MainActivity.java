package com.jonas.middleware;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jonas.middleware.adapter.DeviceAdapter;
import com.jonas.middleware.service.MiddlewareService;
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
        //????????????????????????
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
            //?????????????????????????????????????????????????????????????????????
            if (deviceList.get(position).getBondState() == BluetoothDevice.BOND_NONE) {
                createOrRemoveBond(1, deviceList.get(position));//????????????
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void initPairAdapter() {
        mPairDeviceAdapter = new DeviceAdapter(R.layout.bluetooch_item, pairDeviceList);
        rvPaired.setLayoutManager(new LinearLayoutManager(this));
        rvPaired.setAdapter(mPairDeviceAdapter);
        mPairDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            //?????????????????????????????????????????????????????????????????????
            if (pairDeviceList.get(position).getBondState() == BluetoothDevice.BOND_BONDED) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("address", pairDeviceList.get(position).getAddress());
                startActivity(intent);
            }
        });
        mPairDeviceAdapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            showDialog("???????????????????????????", (dialog, which) -> {
                //????????????
                createOrRemoveBond(2, pairDeviceList.get(position));//????????????
            });
            return false;
        });
    }

    private void permissionsRequest() {
        RxPermissions rxPermissions = new RxPermissions(this);
        Disposable disposable = rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {//????????????
                        registerBluetooth();//?????????????????????
                    } else {//????????????
                        showMsg("???????????????");
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
     * ?????????????????????
     */
    private void searchBoundDevice() {
        if (mBluetoothAdapter == null) {
            showMsg("???????????????");
            return;
        }
        pairDeviceList.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //???????????????????????????0????????????????????????
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //????????????????????????null?????????, ??????????????????
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
     * ????????????????????????
     *
     * @param type   ???????????? 1 ??????  2  ????????????
     * @param device ??????
     */
    private void createOrRemoveBond(int type, BluetoothDevice device) {
        Method method = null;
        try {
            switch (type) {
                case 1://????????????
                    method = BluetoothDevice.class.getMethod("createBond");
                    method.invoke(device);
                    break;
                case 2://????????????
                    method = BluetoothDevice.class.getMethod("removeBond");
                    method.invoke(device);
                    pairDeviceList.remove(device);//?????????????????????????????????????????????
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
        //??????????????????
        if (devices.contains(device)) {
            //????????????????????????null?????????
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
                showMsg("????????????");
                Log.e(TAG, "????????????");
                break;
            case BluetoothDevice.BOND_BONDED:
                addDevice(device, pairDeviceList, mPairDeviceAdapter);
                removeDevice(device, deviceList, mDeviceAdapter);
                showMsg("????????????");
                Log.e(TAG, "????????????");
                break;
            case BluetoothDevice.BOND_BONDING:
                showMsg("????????????");
                Log.e(TAG, "????????????");
                break;
        }
    }

    /**
     * ??????
     *
     * @param dialogTitle     ??????
     * @param onClickListener ?????????????????????
     */
    private void showDialog(String dialogTitle, @NonNull DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogTitle);
        builder.setPositiveButton("??????", onClickListener);
        builder.setNegativeButton("??????", null);
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
                if (mDeviceAdapter != null) {//?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
            showMsg("???????????????");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_BT_CODE) {
            if (resultCode == RESULT_OK) {
                showMsg("??????????????????");
            } else {
                showMsg("??????????????????");
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
                showMsg("???????????????");
                break;
            case BluetoothAdapter.STATE_ON:
                showMsg("???????????????");
                searchBoundDevice();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                showMsg("???????????????...");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                showMsg("???????????????...");
                break;
            default:

                break;
        }
    }

    /**
     * ??????
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //?????????????????????
        unregisterReceiver(mBleReceiver);
    }

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String action = intent.getAction();
            Log.e(TAG, "action: " + action);
            switch (action) {
                // ????????????????????????
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:

                    break;
                // ????????????,??????????????????
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    loadingLay.setVisibility(View.GONE);//??????????????????
                    mDeviceAdapter.changeBondDevice();
                    break;
                // ????????????
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    loadingLay.setVisibility(View.VISIBLE);//??????????????????
                    break;
                // ????????????????????????
                case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:

                    break;
                // Activity ??????????????????
//                case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE :
//
//                    break;
//                // Activity ??????????????????
//                case BluetoothAdapter.ACTION_REQUEST_ENABLE :
//
//                    break;
                // ??????????????????
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:

                    break;
                // Broadcast ??????????????????
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    bluetoothOpenState(intent);
                    break;
                // ??????????????????????????????
                case BluetoothDevice.ACTION_ACL_CONNECTED:

                    break;
                // ????????????????????????????????????
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:

                    break;
                // ??????????????????????????????
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:

                    break;
                // ????????????????????????
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    bluetoothStateChange(device);
                    break;
                // ????????????????????????????????????
                case BluetoothDevice.ACTION_CLASS_CHANGED:

                    break;
                // ???????????????
                case BluetoothDevice.ACTION_FOUND:
                    if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                        addDevice(device, deviceList, mDeviceAdapter);
                    }
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:

                    break;
                // ??????????????????
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
