package com.jonas.middleware.utils;

import java.io.UnsupportedEncodingException;

public class DataProtocolUtil {
    private static final String CHARSET_NAME = "utf-8";

    /**
     * 封包（发送数据）
     * 把发送的数据变成  数组 2进制流
     */
    public static byte[] encodePackage(String data) {
        if(data == null) {
            return new byte[0];
        }else {
            try {
                return data.getBytes(CHARSET_NAME);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }

    /**
     * 解包（接收处理数据）
     * 把网络上数据变成自己想要的数据体
     */
    public static String decodePackage(byte[] netData) {
        if(netData == null) {
            return "";
        }else {
            try {
                return new String(netData, CHARSET_NAME);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}