package com.aio.usbserialport.device

import android.content.Context
import com.aio.usbserialport.parser.Parser
import kotlin.concurrent.thread


/**
 * Created by hd on 2017/9/4 .
 * others unknown device
 */
abstract class OthersDevice(context: Context, aioDeviceType:Int,parser: Parser)
    : Device(context,aioDeviceType, parser){

    abstract fun asyncMeasure()

    override fun measure() {
        thread { asyncMeasure() }
    }
}