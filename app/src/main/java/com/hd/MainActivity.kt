package com.hd

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.aio.usbserialport.listener.ReceiveResultListener
import com.aio.usbserialport.method.AIODeviceMeasure
import com.aio.usbserialport.result.ParserResult
import com.hd.serialport.utils.L
import com.hd.usbserialport.sample.AIODeviceType


/**
 * Created by hd on 2017/8/29 .
 *
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        AIODeviceMeasure.with(AIODeviceType.DEBUG_DEVICE, object : ReceiveResultListener {
            override fun receive(parserResult: ParserResult) {
                L.d("receive : $parserResult")
            }

            override fun error(msg: String) {
                L.d("error : $msg")
            }
        }).startMeasure()
    }

    override fun onPause() {
        super.onPause()
        AIODeviceMeasure.stopMeasure()
    }
}
