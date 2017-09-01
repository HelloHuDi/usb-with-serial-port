package com.hd;

import android.app.Application;

import com.siheal.usbserialport.method.AIODeviceMeasure;


/**
 * Created by hd on 2017/8/29 .
 *
 */
public class AIOApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AIODeviceMeasure.INSTANCE.init(this,BuildConfig.DEBUG);
    }
}
