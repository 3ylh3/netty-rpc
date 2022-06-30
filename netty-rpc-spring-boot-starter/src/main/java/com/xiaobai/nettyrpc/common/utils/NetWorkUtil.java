package com.xiaobai.nettyrpc.common.utils;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * network工具类
 *
 * @author yinzhaojing
 * @date 2022-06-29 18:09:05
 */
public class NetWorkUtil {

    /**
     * 获取本机ip地址
     * @return 本机ip
     */
    public static String getLocalIp() throws Exception {
        String ipAddress = "";
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        while (allNetInterfaces.hasMoreElements()){
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress tmp = addresses.nextElement();
                if (tmp instanceof Inet4Address && !tmp.isLoopbackAddress()
                        && !tmp.getHostAddress().contains(CommonConstants.ADDRESS_DELIMITER)){
                    ipAddress = tmp.getHostAddress();
                    break;
                }
            }
        }
        return ipAddress;
    }
}
