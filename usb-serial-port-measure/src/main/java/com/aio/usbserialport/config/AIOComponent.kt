package com.aio.usbserialport.config

import android.content.Context
import com.aio.usbserialport.device.Device
import com.aio.usbserialport.parser.Parser
import com.hd.serialport.param.MeasureParameter
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.usb_driver.UsbSerialPort


/**
 * Created by hd on 2017/9/1 .
 * provide aio component
 */
interface AIOComponent {

    /**
     * allow extension others object of 'Device' [com.aio.usbserialport.device.OthersDevice]
     */
    fun getOthersDevice(context: Context, type:Int,parser: Parser): Device?

    /**
     * provide measure parameter[SerialPortMeasureParameter] or [com.hd.serialport.param.UsbMeasureParameter]
     */
    fun getMeasureParameter(context: Context, type: Int): MeasureParameter?

    /**
     * provide measure parser[Parser]
     */
    fun getParser(type: Int): Parser?

    /**
     * provide usb port[UsbSerialPort]for measure
     */
    fun getUsbSerialPort(context: Context, type: Int): UsbSerialPort?

    /**
     * provide serial port path for measure
     */
    fun getSerialPortPath(context: Context, type: Int): String?

    /**
     * provide initialization instruct at start measure stage
     */
    fun getInitializationInstructInstruct(type: Int): List<ByteArray>?

    /**
     * provide release instruct at end measure stage
     */
    fun getReleaseInstruct(type: Int): List<ByteArray>?
}