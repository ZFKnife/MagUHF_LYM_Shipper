package com.fxb.shipper.myapplication.presenter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fxb.shipper.myapplication.application.App;
import com.fxb.shipper.myapplication.config.RequestConfig;
import com.fxb.shipper.myapplication.util.Util;
import com.fxb.shipper.myapplication.view.IShipperWriteView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import hardware.print.BarcodeUtil;
import hardware.print.printer;

/**
 * Created by Administrator on 2017/8/9 0009.
 */

public class ShipperWritePresenter extends Presenter {

    private IShipperWriteView iShipperWriteView;

    private String[] strings;

    private Bitmap imageBitmap = null;

    private String shipperPi = null;

    private String shipperMao = null;

    private String shipperjing = null;

    private String carnum = null;

    private printer mPrinter = new printer();

    private String oradid = "";
    private String cargo = "";


    public ShipperWritePresenter(IShipperWriteView iShipperWriteView) {
        super(iShipperWriteView);
        this.iShipperWriteView = iShipperWriteView;
        mPrinter.Open();
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
        sb.append(iShipperWriteView.getLocalhostName()).append(",");
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
        try {
            carnum = URLEncoder.encode(strings[1].substring(0, 1), "UTF-8") + strings[1].substring(1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (str.substring(16, 17).equals("0")) {
            iShipperWriteView.setResult(str.substring(0, 16));
        } else {
            iShipperWriteView.setResult(str);
        }
        contrastShipper();
    }

    @Override
    void writeResponse() {
//        iShipperWriteView.showToast("写入成功！");
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
    private void upShipperMeadData(final String maoWeight, final String piWeight, String jingWeight, final String sb) {
        StringBuilder stringBuilder = new StringBuilder(RequestConfig.upShipperMeasData);
        stringBuilder.append("CARDNUM=").append(strings[0]);
        stringBuilder.append("&CARNUM=").append(carnum);
        stringBuilder.append("&SHIPPERMAO=").append(maoWeight);
        stringBuilder.append("&SHIPPERPI=").append(piWeight);
        stringBuilder.append("&SHIPPERJING=").append(jingWeight);
        Log.i(" ---- ", "upShipperMeadData: " + stringBuilder.toString());
        StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.i(" --- ", "onResponse: " + s);
                if (TextUtils.isEmpty(s)) {
                    iShipperWriteView.showToast("服务器数据异常");
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    if (o.getString("status").equals("0")) {
                        JSONArray ja = new JSONArray(o.getString("data"));
                        JSONObject jo = new JSONObject(String.valueOf(ja.getJSONObject(0)));
                        oradid = jo.getString("ordered");
                        cargo = jo.getString("name");
                        //打印票据
                        printe();

                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(sb);
                        sb2.append(cargo).append(",");
                        sb2.append(maoWeight).append(",");
                        sb2.append(piWeight).append(",");
                        final String data = sb2.toString();
                        Log.i("---", "onResponse: " + data);
                        if (imageBitmap != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //上传发货方图片
                                    uploadShipperServer(RequestConfig.uploadShipperurl, carnum, getBitmapPath(), imageBitmap, data);
                                }
                            }).start();
                        } else {
                            writeTrue(data);
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
    private boolean uploadShipperServer(String targetUrl, String carnum, String fileName, Bitmap bm, String sb) {
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
                writeTrue(sb);
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

    public void printe() {
        if (oradid.equals("")) {
            iShipperWriteView.showToast("请先写卡");
            return;
        }
        if (cargo.equals("")) {
            iShipperWriteView.showToast("请先写卡");
            return;
        }
        if (strings == null) {
            iShipperWriteView.showToast("请先读卡");
            return;
        }
        if (shipperMao == null) {
            iShipperWriteView.showToast("请先读卡");
            return;
        }
        if (shipperPi == null) {
            iShipperWriteView.showToast("请先读卡");
            return;
        }
        if (shipperjing == null) {
            iShipperWriteView.showToast("请先读卡");
            return;
        }

        mPrinter.PrintStringEx("卡的信息平台单据", 40, false, true, printer.PrintType.Centering);
        mPrinter.PrintLineInit(20);
        mPrinter.PrintLineString("发货方单据", 20, 210, false);
        mPrinter.PrintLineEnd();
        String str = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
        mPrinter.PrintLineInit(18);
        mPrinter.PrintLineStringByType(str, 18, printer.PrintType.Centering, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("订单号：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(oradid, 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("发货方：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(iShipperWriteView.getLocalhostName(), 20, 210, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("车牌号：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(strings[1], 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("货物名称：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(cargo, 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("毛重：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(shipperMao, 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("皮重：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(shipperPi, 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("净重：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(shipperjing, 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(25);
        mPrinter.PrintLineStringByType("打印时间：", 24, printer.PrintType.Left, true);
        mPrinter.PrintLineString(Util.getTime(), 20, 200, false);
        mPrinter.PrintLineEnd();
        mPrinter.PrintLineInit(18);
        mPrinter.PrintLineStringByType(str, 18, printer.PrintType.Centering, false);
        mPrinter.PrintLineEnd();
        Bitmap bm = null;
        try {
            bm = BarcodeUtil.encodeAsBitmap("Thanks for using our Android terminal",
                    BarcodeFormat.QR_CODE, 160, 160);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (bm != null) {
            mPrinter.PrintBitmap(bm);
        }
        mPrinter.PrintLineInit(40);
        mPrinter.PrintLineStringByType("", 24, printer.PrintType.Right, true);//160
        mPrinter.PrintLineEnd();
        mPrinter.printBlankLine(40);
        clear();
    }

    private void clear() {
        oradid = "";
        strings = null;
        cargo = "";
        shipperMao = null;
        shipperPi = null;
        shipperjing = null;
        abstractUHFModel.clear();
        iShipperWriteView.setEPCtext("");
        iShipperWriteView.setCarNumText("");
        iShipperWriteView.setShipperJingText("");
        iShipperWriteView.setShipperJingText("");
        iShipperWriteView.showToast("数据写入完成，可进行下一业务操作！");
    }

    public void Step() {
        if (mPrinter == null) {
            return;
        }
        mPrinter.Step((byte) 0x5f);
    }

    public void cancel() {
        mPrinter.Close();
        App.getRequestQueue().cancelAll(this);
    }

}
