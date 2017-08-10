package com.fxb.shipper.myapplication.config;

/**
 *
 * Created by zf on 2017/8/10 0010.
 */

public class RequestConfig {

    private static String ip = "39.108.0.144";

    private static String backPackage = "YJYNLogisticsSystem";

    private static String base = "http://" + ip + "/" + backPackage + "/";

    public static String upShipperMeasData = base + "appPublishInformation?action=upShipperMeasData&";

    public static String getRealordShipper = base + "appPublishInformation?action=getRealordShipper&";

    public static String uploadShipperurl = base + "";

    public static String getShipperList = base + "appPublishInformation?action=getShipperList";


}
