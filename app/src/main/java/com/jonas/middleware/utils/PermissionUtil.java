package com.jonas.middleware.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    public static final int PERMISSION_REQUEST_CODE = 0;
    /**
     * @param permissions
     * @since 2.5.0
     */
    public static boolean checkPermissions(Activity context, String... permissions) {
        List<String> needRequestPermissionList = findDeniedPermissions(context, permissions);
        if (needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(context,
                    needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]),
                    PERMISSION_REQUEST_CODE);
            return true;
        }
        return false;
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private static List<String> findDeniedPermissions(Activity context, String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(context,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    context, perm)) {
                needRequestPermissionList.add(perm);
            }
        }
        return needRequestPermissionList;
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults 权限状态
     * @return 权限
     * @since 2.5.0
     */
    public static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
