package com.hd;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hd.serialport.utils.L;
import com.siheal.usbserialport.config.AIODeviceType;
import com.siheal.usbserialport.listener.ReceiveResultListener;
import com.siheal.usbserialport.method.AIODeviceMeasure;
import com.siheal.usbserialport.result.ParserResult;


/**
 * Created by hd on 2017/8/29 .
 *
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L.INSTANCE.d("onCreate");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        L.INSTANCE.d("onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.INSTANCE.d("onResume");
        AIODeviceMeasure.INSTANCE.with(AIODeviceType.INSTANCE.getDEBUG_DEVICE(), new ReceiveResultListener() {
            @Override
            public void receive(@NonNull ParserResult parserResult) {
                L.INSTANCE.d("receive : "+parserResult.toString());
            }

            @Override
            public void error(@NonNull String msg) {
                L.INSTANCE.d("error : "+msg);
            }
        }).startMeasure();
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.INSTANCE.d("onPause");
        AIODeviceMeasure.INSTANCE.stopMeasure();
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.INSTANCE.d("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.INSTANCE.d("onDestroy");
    }
}
