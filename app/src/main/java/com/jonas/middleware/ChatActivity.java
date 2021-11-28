package com.jonas.middleware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jonas.middleware.adapter.ChatAdapter;
import com.jonas.middleware.bean.UserBean;
import com.jonas.middleware.utils.ChatController;
import com.jonas.middleware.utils.MessageConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ChatActivity";
    private RecyclerView mRv;
    private Button btBack;
    private Button btSend;
    private EditText etInput;
    private ChatAdapter mChatAdapter;
    private String userName;
    private MyHandler myHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private final List<UserBean> users = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        myHandler = new MyHandler();
        initBluetooth();
        initView();
        initAdapter();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String mac = getIntent().getStringExtra("address");
        BluetoothDevice mBluetoothDevice = getBoundDevice(mac).orElse(null);
        if (mBluetoothDevice != null && mBluetoothAdapter != null) {
            ChatController.getInstance().waitingFriends(mBluetoothAdapter, myHandler);
        }
    }

    private Optional<BluetoothDevice> getBoundDevice(String mac) {
        if (mBluetoothAdapter == null) {
            showMsg("不支持蓝牙");
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //如果获取的结果大于0，则开始逐个解析
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //过滤掉设备名称为null的设备, 防止重复添加
                if (TextUtils.equals(mac, device.getAddress())) {
                    userName = device.getName();
                    return Optional.of(device);
                }
            }
        }
        return Optional.empty();
    }

    private void initAdapter() {
        mChatAdapter = new ChatAdapter(R.layout.chat_item, users);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(mChatAdapter);
        mChatAdapter.setOnItemChildClickListener((adapter, view, position) -> {

        });
    }

    private void initView() {
        mRv = findViewById(R.id.rv_text);
        btBack = findViewById(R.id.bt_back);
        btBack.setOnClickListener(this);
        btSend = findViewById(R.id.bt_send);
        btSend.setOnClickListener(this);
        etInput = findViewById(R.id.et_input);
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v == btBack) {
            finish();
        } else if (v == btSend) {
            Editable editable = etInput.getText();
            if (editable != null) {
                String text = etInput.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    UserBean userBean = getUserBean(text, "", ChatAdapter.USER_TYPE_OWN);
                    users.add(userBean);
                    mChatAdapter.changeBondDevice();
                    ChatController.getInstance().writeMessage(text);
                    etInput.setText("");
                } else {
                    showMsg("不能发送空消息");
                }
            }

        }
    }

    private UserBean getUserBean(String text, String name, int userType) {
        UserBean userBean = new UserBean();
        userBean.setChatText(text);
        userBean.setName(name);
        userBean.setSendTime(System.currentTimeMillis());
        userBean.setUserType(userType);
        return userBean;
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "what = " + msg.what);

            if (msg.what == MessageConstant.MSG_READ_DATA) {
                UserBean userBean = getUserBean((String) msg.obj, userName, ChatAdapter.USER_TYPE_OTHERS);
                users.add(userBean);
                mChatAdapter.changeBondDevice();
            } else {

            }
        }
    }
}