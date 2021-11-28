package com.jonas.middleware.adapter;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jonas.middleware.R;
import com.jonas.middleware.bean.UserBean;

import java.util.List;

public class ChatAdapter extends BaseQuickAdapter<UserBean, BaseViewHolder> {
    public static final int USER_TYPE_OWN = 0;
    public static final int USER_TYPE_OTHERS = 1;

    public ChatAdapter(int layoutResId, @Nullable List<UserBean> data) {
        super(layoutResId, data);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserBean item) {
        if (item.getUserType() == USER_TYPE_OWN) {
            helper.setVisible(R.id.iv_right_hear, true);
            helper.setVisible(R.id.iv_left_hear, false);
        } else {
            helper.setVisible(R.id.iv_right_hear, false);
            helper.setVisible(R.id.iv_left_hear, true);
        }

        helper.setText(R.id.tv_name, item.getName());
        helper.setText(R.id.tv_chat, item.getChatText());
        TextView textView = helper.getView(R.id.tv_chat);
        textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //这个回调会调用多次，获取完行数记得注销监听
                textView.getViewTreeObserver().removeOnPreDrawListener(this);
                if (textView.getLineCount() == 1 && item.getUserType() == USER_TYPE_OWN) {
                    textView.setGravity(Gravity.RIGHT);
                } else {
                    textView.setGravity(Gravity.LEFT);
                }
                Log.e(TAG, "TextView 行数：" + textView.getLineCount());
                return false;
            }
        });
        helper.addOnClickListener(R.id.rl_chat);
    }

    /**
     * 刷新适配器
     */
    public void changeBondDevice(){
        notifyDataSetChanged();
    }
}
