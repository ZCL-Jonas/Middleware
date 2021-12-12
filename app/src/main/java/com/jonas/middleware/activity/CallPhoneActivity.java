package com.jonas.middleware.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jonas.middleware.R;
import com.jonas.middleware.adapter.ContactAdapter;
import com.jonas.middleware.monitor.call.ContactBean;
import com.jonas.middleware.utils.ContactHelper;
import com.jonas.middleware.utils.ContactUtils;
import com.jonas.middleware.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class CallPhoneActivity extends AppCompatActivity {
    private static final String TAG = "CallPhoneActivity";
    private final List<ContactBean> contactBeans = new ArrayList<>();
    private ContactAdapter contactAdapter;
    private PhoneReceiver mPhoneReceiver;

    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
//            Manifest.permission.DISABLE_KEYGUARD,
//            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
    };

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            onCallStateChanged(state, phoneNumber);
        }
    };

    private void onCallStateChanged(int state, String phoneNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.e(TAG, "************* 空闲状态中 ************");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.e(TAG, "************* 通话中 ************");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.e(TAG, "************* 响铃中 ************");
                break;
            default:
                break;
        }
        Log.e(TAG, " phoneNumber : " + phoneNumber);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_phone);
        initAdapter();
        initReceiver();
        initTelephone();
    }

    private void initReceiver() {
        mPhoneReceiver = new PhoneReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(mPhoneReceiver, intentFilter);
    }

    private void initTelephone() {
        Object object = getSystemService(TELEPHONY_SERVICE);
        if (object instanceof TelephonyManager) {
            TelephonyManager mTelephonyManager = (TelephonyManager) object;
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!PermissionUtil.checkPermissions(this, needPermissions)) {
            getPhoneContact();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPhoneReceiver);
    }

    private void getPhoneContact() {
//        List<ContactBean> lists = ContactHelper.getInstance().getContacts(this);
        List<ContactBean> lists = ContactUtils.getPhoneContact(this);
        contactBeans.clear();
        contactBeans.addAll(lists);
        contactAdapter.notifyDataSetChanged();
    }

    private void initAdapter() {
        RecyclerView recyclerView = findViewById(R.id.rl_contact);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(R.layout.item_contact, contactBeans);
        recyclerView.setAdapter(contactAdapter);
        contactAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            String phoneNumber = contactBeans.get(position).getNumber();
            Intent intent = new Intent();
            if (R.id.iv_call == view.getId()) {
                intent.setAction(Intent.ACTION_CALL);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("tel:" + phoneNumber));
            } else if (R.id.iv_sms == view.getId()) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:" + phoneNumber));
            }
            startActivity(intent);
        });
    }

    /**
     * requestPermissions的回调
     * 一个或多个权限请求结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE) {
            if (!PermissionUtil.verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
            } else {
                getPhoneContact();
            }
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notifyTitle);
        builder.setMessage(R.string.notifyMsg);
        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> finish());
        builder.setPositiveButton(R.string.setting, (dialog, which) -> startAppSettings());
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private class PhoneReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "action : " + action);
            switch (action) {
                case Intent.ACTION_NEW_OUTGOING_CALL:
                    initTelephone();
                    break;
                case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                    // 来电去电都会走
                    // 获取当前电话状态
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Log.e(TAG, "phoneNumber : " + number);
                    break;
                default:
                    break;
            }
        }
    }
}