package com.aio.usbserialport.device

import android.content.Context
import com.aio.usbserialport.parser.DataPackageEntity
import com.aio.usbserialport.parser.Parser
import com.hd.serialport.listener.UsbMeasureListener
import com.hd.serialport.method.DeviceMeasureController
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.usb_driver.UsbSerialPort
import java.util.*

/**
 * Created by hd on 2017/8/28 .
 *
 */
open class UsbPortDevice(context: Context, aioDeviceType:Int, private val port: UsbSerialPort? = null, private val parameter: UsbMeasureParameter, parser: Parser)
    : Device(context,aioDeviceType, parser), UsbMeasureListener {

    override fun write(tag: Any?,usbSerialPort: UsbSerialPort) {
        usbSerialPortList.add(usbSerialPort)
    }

    override fun measure() {
        DeviceMeasureController.measure(usbSerialPort = port, parameter = parameter, listener = this)
    }

    override fun release() {
    }

    override fun measureError(tag: Any?,message: String) {
        error(message)
    }

    override fun measuring(tag: Any?,usbSerialPort: UsbSerialPort, data: ByteArray) {
        dataQueue.put(DataPackageEntity(port = usbSerialPort, data = data))
        addLogcat(Arrays.toString(data))
    }

}