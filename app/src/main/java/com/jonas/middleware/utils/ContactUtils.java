package com.jonas.middleware.utils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.Log;

import com.jonas.middleware.monitor.call.ContactBean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactUtils {
    private static final String TAG = "ContactUtils";

    /**
     * 查询的字段
     */
    private static final String[] PHONES_PROJECTION = new String[]{
            Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_ID, Phone.CONTACT_ID
    };

    /**
     * 联系人名称
     */
    private static final int PHONES_DISPLAY_NAME = 0;
    /**
     * 电话号
     */
    private static final int PHONES_NUMBER = 1;
    /**
     * 头像ID
     */
    private static final int PHONES_PHOTO_ID = 2;
    /**
     * 联系人ID
     */
    private static final int PHONES_CONTACT_ID = 3;

    public static String getDisplayNameByNumber(Context context, String number) {
        Cursor cursor = null;
        String displayName = "";
        ContentResolver contentResolver = context.getContentResolver();
        String selection = Phone.NUMBER + "like ?";
        String[] selectionArgs = new String[]{"%" + number + "%"};
        try {
            cursor = contentResolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, selection, selectionArgs, "sort_key");
            if (cursor != null) {
                final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberNoIndex = cursor.getColumnIndex(Phone.NUMBER);
                String numberNo = "";
                while (cursor.moveToNext()) {
                    displayName = cursor.getString(displayNameIndex);
                    numberNo = cursor.getString(numberNoIndex);
                    Log.e(TAG, "displayName : " + displayName + " , numberNo : " + numberNo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return displayName;
    }

    /**
     * 获取通讯录
     * @param context 环境变量
     * @return 列表
     */
    public static List<ContactBean> getPhoneContact(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        List<ContactBean> contactBeans = new ArrayList<>();
        try (Cursor cursor = contentResolver.query(
                Phone.CONTENT_URI, PHONES_PROJECTION,
                null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(PHONES_DISPLAY_NAME);
                    if (TextUtils.isEmpty(displayName)) {
                        continue;
                    }
                    ContactBean contactBean = new ContactBean();
                    contactBean.setDisplayName(displayName);
                    String number = cursor.getString(PHONES_NUMBER);
                    contactBean.setNumber(number.replace(" ", ""));
                    long phoneId = cursor.getLong(PHONES_PHOTO_ID);
                    long contactId = cursor.getLong(PHONES_CONTACT_ID);
                    // 得到联系人头像Bitamp
                    Bitmap contactPhoto = null;
                    if (phoneId > 0) {
                        Uri uri = ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI, contactId);
                        InputStream input = ContactsContract.Contacts
                                .openContactPhotoInputStream(contentResolver, uri);
                        contactPhoto = BitmapFactory.decodeStream(input);
                    }
                    contactBean.setImg(contactPhoto);
                    contactBeans.add(contactBean);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "error : " + e.getMessage());
        }
        return contactBeans;
    }

    /**
     * 得到手机SIM卡联系人人信息
     **/
    private void getSIMContacts(Context context) {
        ContentResolver resolver = context.getContentResolver();
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        try (Cursor phoneCursor = resolver.query(
                uri, PHONES_PROJECTION, null, null, null)) {
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {

                    // 得到手机号码
                    String phoneNumber = phoneCursor.getString(PHONES_NUMBER);
                    // 当手机号码为空的或者为空字段 跳过当前循环
                    if (TextUtils.isEmpty(phoneNumber))
                        continue;
                    // 得到联系人名称
                    String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME);

                    // Sim卡中没有联系人头像
                    ContactBean contactBean = new ContactBean();
                    contactBean.setDisplayName(contactName);
                    contactBean.setNumber(phoneNumber.replace(" ", ""));
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "error : " + e.getMessage());
        }
    }
}
