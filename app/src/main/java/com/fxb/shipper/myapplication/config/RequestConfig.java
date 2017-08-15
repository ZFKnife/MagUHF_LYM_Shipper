package com.fxb.shipper.myapplication.config;

/**
 *
 * Created by zf on 2017/8/10 0010.
 */

public class RequestConfig {

    private static String ip = "139.224.0.153";

    private static String backPackage = "LYMLogisticsSystem";

    private static String base = "http://" + ip + "/" + backPackage + "/";

    public static String upShipperMeasData = base + "appPublishInformation?action=upShipperMeasData&";

    public static String getRealordShipper = base + "appPublishInformation?action=getRealordShipper&";

    public static String uploadShipperurl = base + "appUser?action=uploadShipperorder";

    public static String getShipperList = base + "appPublishInformation?action=getShipperList";


}
