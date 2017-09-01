package com.siheal.usbserialport.config

import android.content.Context
import com.hd.serialport.param.MeasureParameter
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.usb_driver.UsbSerialPort
import com.siheal.usbserialport.cache.UsbSerialPortCache
import com.siheal.usbserialport.parser.Parser
import com.siheal.usbserialport.parser.TestSerialPortParser


/**
 * Created by hd on 2017/8/29 .
 * measure component,add or modification according to the actual situation
 */
object AIOComponent {

    /**
     * provide measure parameter[SerialPortMeasureParameter] or [com.hd.serialport.param.UsbMeasureParameter]
     */
    fun getMeasureParameter(context: Context, type: Int): MeasureParameter? {
        if(type==AIODeviceType.UNKNOWN_DEVICE)throw NullPointerException("unknown aio type")
        var parameter: MeasureParameter? = null
        val path=getSerialPortPath(context,type)
        when (type) {
            AIODeviceType.DEBUG_DEVICE -> parameter = SerialPortMeasureParameter(path)
        }
        return parameter
    }

    /**
     * provide measure parser[Parser]
     */
    fun getParser(type: Int): Parser? {
        var parse: Parser? = null
        when (type) {
            AIODeviceType.UNKNOWN_DEVICE -> throw NullPointerException("unknown aio type")
            AIODeviceType.DEBUG_DEVICE -> parse = TestSerialPortParser()
        }
        return parse
    }

    /**
     * provide usb port[UsbSerialPort]for measure
     */
    fun getUsbSerialPort(context: Context, type: Int): UsbSerialPort? {
        if(type==AIODeviceType.UNKNOWN_DEVICE)throw NullPointerException("unknown aio type")
        return UsbSerialPortCache.newInstance(context,type).getUsbPortCache()
    }

    /**
     * provide serial port path for measure
     */
    fun getSerialPortPath(context: Context, type: Int):String?{
        val path:String?
        when(type){
            AIODeviceType.UNKNOWN_DEVICE -> throw NullPointerException("unknown aio type")
            AIODeviceType.DEBUG_DEVICE -> path="/dev/ttyS3"
            else->path=UsbSerialPortCache.newInstance(context,type).getSerialPortCache()
        }
        return path
    }

    /**
     * provide initialization instruct at start measure stage
     */
    fun getInitializationInstructInstruct(type:Int):List<ByteArray>?{
        var instructList:List<ByteArray>?=null
        when(type){
            AIODeviceType.UNKNOWN_DEVICE->throw NullPointerException("unknown aio type")
            AIODeviceType.DEBUG_DEVICE -> instructList=listOf(byteArrayOf(2.toByte(),15.toByte(),6.toByte()),byteArrayOf(7.toByte(),34.toByte(),23.toByte()))
        }
        return instructList
    }

    /**
     * provide release instruct at end measure stage
     */
    fun getReleaseInstruct(type:Int):List<ByteArray>?{
        var instructList:List<ByteArray>?=null
        when(type){
            AIODeviceType.UNKNOWN_DEVICE -> throw NullPointerException("unknown aio type")
        }
        return instructList
    }

}
