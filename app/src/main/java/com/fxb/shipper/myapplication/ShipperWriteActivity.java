package com.fxb.shipper.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fxb.shipper.myapplication.presenter.ShipperWritePresenter;
import com.fxb.shipper.myapplication.util.Sp;
import com.fxb.shipper.myapplication.view.IShipperWriteView;


/**
 * Created by dxl on 2017-06-24.
 */

public class ShipperWriteActivity extends Activity implements IShipperWriteView {
    private static final String TAG = "ShipperWriteActivity";


    private TextView tv_shipperEpc;
    private TextView tv_shipperCarNum;
    private EditText et_maoZhong;
    private EditText et_piZhong;
    private TextView tv_jingZhong;
    private TextView tv_resultView;

    private Button btn_shipperReadEpc;
    private Button btn_shipperReading;
    private Button btn_shipperWritting;


    private ShipperWritePresenter presenter = null;


    private ImageView imageView;
    private Bitmap imageBitmap;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shipper_write);
        initView();
        presenter = new ShipperWritePresenter(this);

        presenter.setEPCtext();

        onClick();
    }

    private void initView() {

        tv_shipperEpc = (TextView) findViewById(R.id.tv_shipper_epc);
        tv_shipperCarNum = (TextView) findViewById(R.id.tv_shipper_carnum);
        et_maoZhong = (EditText) findViewById(R.id.et_maozhong);
        et_piZhong = (EditText) findViewById(R.id.et_pizhong);
        tv_jingZhong = (TextView) findViewById(R.id.tv_jingzhong);
        tv_resultView = (TextView) findViewById(R.id.tv_resultView);

        imageView = (ImageView) findViewById(R.id.shipper_image);

        btn_shipperReadEpc = (Button) findViewById(R.id.btn_shipper_readepc);
        btn_shipperReading = (Button) findViewById(R.id.btn_shipper_reading);
        btn_shipperWritting = (Button) findViewById(R.id.btn_shipper_writting);

    }

    private void onClick() {
        btn_shipperReadEpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setEPCtext();
            }
        });
        btn_shipperReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.readCarNum();
            }
        });
        btn_shipperWritting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.writeShipper();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            imageBitmap = (Bitmap) bundle.get("data");
            assert imageBitmap != null;
            imageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void setEPCtext(String EPC) {
        tv_shipperEpc.setText(EPC);
    }

    @Override
    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setDialog(String str) {

    }

    @Override
    public String getShipperPI() {
        return et_piZhong.getText().toString().trim();
    }

    @Override
    public String getShipperMao() {
        return et_maoZhong.getText().toString().trim();
    }

    @Override
    public void setCarNumText(String str) {
        tv_shipperCarNum.setText(str);
    }

    @Override
    public void setShipperJingText(String str) {
        tv_jingZhong.setText(str);
    }

    @Override
    public String getLocalhostName() {
        return Sp.getStrings(ShipperWriteActivity.this, "name");
    }

    @Override
    public void finash() {
        finish();
    }

    @Override
    public void setResult(String str) {
        tv_resultView.setText(str);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.cancel();
    }
}
