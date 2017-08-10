package com.fxb.shipper.myapplication.presenter;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fxb.shipper.myapplication.application.App;
import com.fxb.shipper.myapplication.config.RequestConfig;
import com.fxb.shipper.myapplication.util.Sp;
import com.fxb.shipper.myapplication.util.Util;
import com.fxb.shipper.myapplication.view.IShipperWriteView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

/**
 * Created by Administrator on 2017/8/9 0009.
 */

public class ShipperWritePresenter extends Presenter {

    private IShipperWriteView iShipperWriteView;

    private String[] strings;

    private Bitmap imageBitmap = null;

    private String shipperPi;

    private String shipperMao;

    private String shipperjing;

    private String carnum = null;

    public ShipperWritePresenter(IShipperWriteView iShipperWriteView) {
        super(iShipperWriteView);
        this.iShipperWriteView = iShipperWriteView;
    }

    public void readCarNum() {
        readTrue(0, 32);
    }

    public void writeShipper() {
        shipperPi = iShipperWriteView.getShipperPI();
        StringBuilder sb = new StringBuilder();
        if (shipperPi.equals("")) {
            iShipperWriteView.showToast("发货皮重不可以为空");
            return;
        }
        shipperMao = iShipperWriteView.getShipperMao();
        if (shipperMao.equals("")) {
            iShipperWriteView.showToast("发货毛重不可以为空");
            return;
        }
        if (strings == null || strings.length == 0) {
            iShipperWriteView.showToast("车牌号不能为空,请先读卡");
            return;
        }
        Double d_rough_weight = Double.parseDouble(shipperMao);
        Double d_tare = Double.parseDouble(shipperPi);
        double d_weight_empty = d_rough_weight - d_tare;
        DecimalFormat df = new DecimalFormat("#.00");
        shipperMao = df.format(d_rough_weight);
        shipperPi = df.format(d_tare);
        shipperjing = df.format(d_weight_empty);
        sb.append(strings[0]).append(",");
        sb.append(strings[1]).append(",");
        sb.append(shipperMao).append(",");
        sb.append(shipperPi).append(",");
        sb.append(shipperjing).append(",");
        sb.append(Sp.getStrings(App.mContext, "name")).append(",");
        iShipperWriteView.setShipperJingText(shipperjing);
//        writeTrue(sb.toString());
        //上传发货方信息 上传成功后写卡
        upShipperMeadData(shipperMao, shipperPi, shipperjing, sb.toString());
    }

    @Override
    void readResponse(String str) {
        strings = str.split(",");
        iShipperWriteView.showToast("读取成功！");
        iShipperWriteView.setCarNumText(strings[1]);
        carnum = URLEncoder.encode(strings[1].substring(0, 1)) + strings[1].substring(1);
        iShipperWriteView.setResult(str);

        contrastShipper();
    }

    @Override
    void writeResponse() {
        iShipperWriteView.showToast("写入成功！");
        iShipperWriteView.setResult("完成");
    }


    /**
     * 核对发货方
     */
    private void contrastShipper() {
        if (strings == null) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(RequestConfig.getRealordShipper);
        stringBuilder.append("&CARNUM=").append(carnum);
        StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (TextUtils.isEmpty(s)) {
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    if (o.getString("status").equals("0")) {
                        String localname = iShipperWriteView.getLocalhostName();
                        String intentname = o.getString("name");
                        if (!localname.equals(intentname)) {
                            iShipperWriteView.showToast("所在发货商和订单发货商不符！");
                            iShipperWriteView.finash();
                        } else {
                            iShipperWriteView.showToast("通过！");
                        }
                    } else if (o.getString("status").equals("1")) {
                        iShipperWriteView.showToast("请确认是否接单！");
                        iShipperWriteView.finash();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        getContactRequest.setTag(this);
        App.getRequestQueue().add(getContactRequest);
    }

    /**
     * 上传发货方信息
     */
    private void upShipperMeadData(String maoWeight, String piWeight, String jingWeight, final String sb) {
        //上传计量数据
            /*
            * 参数说明
            * 139.224.0.153：IP
            * LYMistSystem：项目名
            * appPublishInformation：类名
            * upShipperMeasData：action
            * CARDNUM：卡编号
            * CARNUM：车牌号
            * SHIPPERMAO：发货端毛重
            * SHIPPERPI：发货端皮重
            * SHIPPERJING：发货端净重
            * */
        StringBuilder stringBuilder = new StringBuilder(RequestConfig.upShipperMeasData);
        stringBuilder.append("CARDNUM=").append(strings[0]);
        stringBuilder.append("&CARNUM=").append(carnum);
        stringBuilder.append("&SHIPPERMAO=").append(maoWeight);
        stringBuilder.append("&SHIPPERPI=").append(piWeight);
        stringBuilder.append("&SHIPPERJING=").append(jingWeight);
        StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (TextUtils.isEmpty(s)) {
                    iShipperWriteView.showToast("服务器数据异常");
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    if (o.getString("status").equals("0")) {
                        writeTrue(sb);
                        if (imageBitmap != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //上传发货方图片
                                    uploadShipperServer(RequestConfig.uploadShipperurl, carnum, getBitmapPath(), imageBitmap);
                                }
                            }).start();
                        }
                    }
                    iShipperWriteView.showToast(o.getString("msg"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                iShipperWriteView.showToast("网络异常，请稍后再试");

            }
        });
        getContactRequest.setTag(this);
        App.getRequestQueue().add(getContactRequest);
    }

    /**
     * 生成图片名
     *
     * @return
     */
    private String getBitmapPath() {
        return "shipper" + System.currentTimeMillis() + ".jpg";
    }

    /**
     * 上传图片
     *
     * @param targetUrl
     * @param carnum
     * @param fileName
     * @param bm
     * @return
     */
    private boolean uploadShipperServer(String targetUrl, String carnum, String fileName, Bitmap bm) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(targetUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"CARNUM\"" + end);
            dos.writeBytes(end);
            dos.writeBytes(carnum + end);

            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"imagePath\"; filename=\"" + fileName + "\"" + end);
            dos.writeBytes(end);

            dos.write(Util.Bitmap2Bytes(bm));
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            JSONObject resultJson = new JSONObject(result);
            int i = resultJson.getInt("status");
            if (i == 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        iShipperWriteView.showToast("上传成功");
                    }
                });
            }
            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                httpURLConnection = null;
            }
        }
        return true;
    }

    public void cancel() {
        App.getRequestQueue().cancelAll(this);
    }

}
