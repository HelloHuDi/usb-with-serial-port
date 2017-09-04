package com.aio.usbserialport.device

import android.content.Context
import com.aio.usbserialport.listener.ReceiveResultListener
import com.aio.usbserialport.parser.Parser


/**
 * Created by hd on 2017/9/4 .
 * others unknown device
 */
open class OthersDevice(context: Context, aioDeviceType:Int,parser: Parser, listener: ReceiveResultListener)
    : Device(context,aioDeviceType, parser, listener){

    override fun measure() {

    }

    override fun release() {

    }

}