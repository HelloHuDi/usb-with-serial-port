package com.hd

import android.app.Application
import com.aio.usbserialport.method.AIODeviceMeasure
import com.hd.usbserialport.sample.AIOComponentHandler


/**
 * Created by hd on 2017/8/29 .
 *
 */
class AIOApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AIODeviceMeasure.init(this, BuildConfig.DEBUG, AIOComponentHandler())
    }
}
