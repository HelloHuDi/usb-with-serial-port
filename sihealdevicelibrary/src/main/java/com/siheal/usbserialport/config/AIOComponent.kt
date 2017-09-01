package com.siheal.usbserialport.config

import android.content.Context
import com.hd.serialport.param.MeasureParameter
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.usb_driver.UsbSerialPort
import com.siheal.usbserialport.parser.Parser


/**
 * Created by hd on 2017/9/1 .
 * provide aio component
 */
interface AIOComponent {

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