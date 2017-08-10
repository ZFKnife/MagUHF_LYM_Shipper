package com.fxb.shipper.myapplication.view;

/**
 * zf 2033152950
 * Created by Administrator on 2017/8/4 0004.
 */

public interface IUHFViewBase {
    /**
     * 设置epc
     * @param EPC
     */
    public void setEPCtext(String EPC);

    /**
     * 显示吐司
     * @param str
     */
    public void showToast(String str);

    /**
     * 显示对话框
     * @param str
     */
    public void setDialog(String str);

}
