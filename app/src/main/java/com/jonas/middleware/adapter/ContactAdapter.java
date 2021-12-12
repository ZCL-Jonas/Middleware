package com.jonas.middleware.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jonas.middleware.R;
import com.jonas.middleware.bean.UserBean;
import com.jonas.middleware.monitor.call.ContactBean;

import java.util.List;

public class ContactAdapter extends BaseQuickAdapter<ContactBean, BaseViewHolder> {

    public ContactAdapter(int layoutResId, @Nullable List<ContactBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ContactBean item) {
        helper.setText(R.id.tv_display_name, item.getDisplayName());
        helper.setText(R.id.tv_number, item.getNumber());
        if (item.getImg() != null) {
            helper.setImageBitmap(R.id.iv_image, item.getImg());
        }
        helper.addOnClickListener(R.id.iv_call);
        helper.addOnClickListener(R.id.iv_sms);
    }
}
