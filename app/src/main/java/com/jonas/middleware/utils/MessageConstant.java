package com.jonas.middleware.utils;

import java.util.UUID;

public class MessageConstant {

    /**
     * 链接服务
     */
    public static final int MSG_CONNECTED_TO_SERVER = 1000;

    /**
     * 读取数据
     */
    public static final int MSG_READ_DATA = 1001;

    /**
     * 开始链接
     */
    public static final int MSG_START_LISTENING = 1002;

    /**
     * 错误
     */
    public static final int MSG_ERROR = 1003;

    /**
     * 链接上客户
     */
    public static final int MSG_GOT_A_CLIENT = 1004;

    /**
     * bt socket closed
     */
    public static final int MSG_BT_SOCKET_CLOSED = 1005;

    /**
     * 连接使用的uuid
     */
    public static final UUID DEVICE_CONNECTION_UUID = UUID.fromString("00001109-0000-1000-8000-00805F9B34FB");

    /**
     * 连接使用的uuid
     */
    public static final UUID DEVICE_SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

}
