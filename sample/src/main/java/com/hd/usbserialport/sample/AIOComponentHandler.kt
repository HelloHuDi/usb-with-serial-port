package com.hd.usbserialport.sample

import android.content.Context
import com.aio.usbserialport.cache.UsbSerialPortCache
import com.aio.usbserialport.config.AIOComponent
import com.aio.usbserialport.device.Device
import com.aio.usbserialport.parser.Parser
import com.hd.serialport.param.MeasureParameter
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.usb_driver.UsbSerialPort


/**
 * Created by hd on 2017/9/2 .
 *
 */
class AIOComponentHandler : AIOComponent {

    override fun getOthersDevice(context: Context, type: Int, parser: Parser): Device? {
        return null
    }

    /**
     * provide measure parameter[SerialPortMeasureParameter] or [com.hd.serialport.param.UsbMeasureParameter]
     */
    override fun getMeasureParameter(context: Context, type: Int): MeasureParameter? {
        if (type == AIODeviceType.UNKNOWN_DEVICE) throw NullPointerException("unknown aio type")
        var parameter: MeasureParameter? = null
        val path = getSerialPortPath(context, type)
        when (type) {
            AIODeviceType.DEBUG_DEVICE -> parameter = SerialPortMeasureParameter(path)
        }
        return parameter
    }

    /**
     * provide measure parser[Parser]
     */
    override fun getParser(type: Int): Parser? {
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
    override fun getUsbSerialPort(context: Context, type: Int): UsbSerialPort? {
        if (type == AIODeviceType.UNKNOWN_DEVICE) throw NullPointerException("unknown aio type")
        return UsbSerialPortCache.newInstance(context, type).getUsbPortCache()
    }

    /**
     * provide serial port path for measure
     */
    override fun getSerialPortPath(context: Context, type: Int): String? {
        return when (type) {
            AIODeviceType.UNKNOWN_DEVICE -> throw NullPointerException("unknown aio type")
            AIODeviceType.DEBUG_DEVICE -> "/dev/ttyS3"
            else -> UsbSerialPortCache.newInstance(context, type).getSerialPortCache()
        }
    }

    /**
     * provide initialization instruct at start measure stage
     */
    override fun getInitializationInstructInstruct(type: Int): List<ByteArray>? {
        var instructList: List<ByteArray>? = null
        when (type) {
            AIODeviceType.UNKNOWN_DEVICE -> throw NullPointerException("unknown aio type")
            AIODeviceType.DEBUG_DEVICE -> instructList = listOf(byteArrayOf(2.toByte(), 15.toByte(), 6.toByte()), byteArrayOf(7.toByte(), 34.toByte(), 23.toByte()))
        }
        return instructList
    }

    /**
     * provide release instruct at end measure stage
     */
    override fun getReleaseInstruct(type: Int): List<ByteArray>? {
        var instructList: List<ByteArray>? = null
        when (type) {
            AIODeviceType.UNKNOWN_DEVICE -> throw NullPointerException("unknown aio type")
        }
        return instructList
    }

}