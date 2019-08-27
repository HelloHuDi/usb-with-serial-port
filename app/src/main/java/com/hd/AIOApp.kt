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
        
        //扩展usb 驱动
        //当pair.first填写成 DriversType下的已知类型(USB_CP21xx,USB_PL2303...)，理论上扩展的driver将会替换库默认的driver
        //所以新增driver的type,最好不要使用DriversType类型
//        val list = listOf<Pair<String, out Class<out UsbSerialDriver>>>(Pair("custom", TestUsbDriver::class.java))
//        UsbExtendDriver.Extender().setDrivers(list as MutableList<Pair<String, Class<out UsbSerialDriver>>>).extend()
    }
}
