package com.jonas.middleware.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jonas.middleware.R;
import com.jonas.middleware.adapter.ContactAdapter;
import com.jonas.middleware.monitor.call.ContactBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiddlewareActivity extends AppCompatActivity {
    private static final Map<String, Class> activityBeans = new HashMap<>();

    static {
        activityBeans.put("聊天", ChatActivity.class);
        activityBeans.put("蓝牙", MainActivity.class);
        activityBeans.put("通讯录", CallPhoneActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_middleware);
        initAdapter();
    }

    private void initAdapter() {
        RecyclerView recyclerView = findViewById(R.id.rl_activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> names = new ArrayList<>(activityBeans.keySet());
        ActivityAdapter contactAdapter = new ActivityAdapter(R.layout.item_default, names);
        contactAdapter.setOnItemChildClickListener((adapter, view, position) ->
                startActivity(new Intent(MiddlewareActivity.this, activityBeans.get(names.get(position)))));
        recyclerView.setAdapter(contactAdapter);
    }

    public static class ActivityAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public ActivityAdapter(int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.bt_activity_name, item);
            helper.addOnClickListener(R.id.bt_activity_name);
        }
    }
}