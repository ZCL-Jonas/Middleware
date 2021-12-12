package com.jonas.middleware.monitor.call;

import android.graphics.Bitmap;

public class ContactBean {
    private String displayName;
    private String number;
    private Bitmap img;

    public ContactBean() {
    }

    public ContactBean(String displayName, String number) {
        this.displayName = displayName;
        this.number = number;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}
