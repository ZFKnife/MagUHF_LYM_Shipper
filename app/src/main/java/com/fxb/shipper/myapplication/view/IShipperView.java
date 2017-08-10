package com.fxb.shipper.myapplication.view;

/**
 * Created by Administrator on 2017/8/9 0009.
 */

public interface IShipperView extends IUHFViewBase {
    /**
     * 获取皮重
     *
     * @return
     */
    public String getShipperPI();

    /**
     * 获取毛重
     *
     * @return
     */
    public String getShipperMao();

    /**
     * 设置车牌号
     *
     * @param str
     */
    public void setCarNumText(String str);

    /**
     * 设置净重
     *
     * @param str
     */
    public void setShipperJingText(String str);

    /**
     * 获取公司名称 本地存储
     *
     * @return
     */
    public String getLocalhostName();

    /**
     * 结束activity
     */
    public void finash();

    /**
     * 设置反馈信息
     */
    public void setResult(String str);
}
