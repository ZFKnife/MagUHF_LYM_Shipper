package com.fxb.shipper.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fxb.shipper.myapplication.config.RequestConfig;
import com.fxb.shipper.myapplication.util.Sp;
import com.fxb.shipper.myapplication.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18 0018.
 */

public class SelectShipperActivity extends Activity {
    private String TAG = SelectShipperActivity.class.getSimpleName();

    private Spinner selectActivityShipper;

    private Button selectActivityBtn;

    private List<String> list;

    private int size = -1;

    private RequestQueue mRequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shipper);

        init();
        getShipper();
    }

    private void init() {
        mRequestQueue = Volley.newRequestQueue(this);
        selectActivityShipper = (Spinner) findViewById(R.id.select_activity_shipper);
        selectActivityBtn = (Button) findViewById(R.id.select_activity_btn);
        selectActivityShipper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                size = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        selectActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list == null) {
                    return;
                }
                if (size == -1) {
                    return;
                }
                Sp.putString(SelectShipperActivity.this, "name", list.get(size));
                Toast.makeText(SelectShipperActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void getShipper() {
        StringRequest getContactRequest = new StringRequest(RequestConfig.getShipperList, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (TextUtils.isEmpty(s)) {
                    ToastUtil.getShortToastByString(SelectShipperActivity.this, "服务器数据异常");
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    JSONArray ja = new JSONArray(o.getString("namelist"));
                    int w = ja.length();
                    list = new ArrayList<>();
                    for (int i = 0; i < w; i++) {
                        list.add(ja.getString(i));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectShipperActivity.this,
                            android.R.layout.simple_expandable_list_item_1, list);
                    selectActivityShipper.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.getShortToastByString(SelectShipperActivity.this, "网络异常，请稍后再试");
            }
        });
        mRequestQueue.add(getContactRequest);
    }
}
