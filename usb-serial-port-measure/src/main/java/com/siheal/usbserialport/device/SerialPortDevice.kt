package com.siheal.usbserialport.device

import android.content.Context
import com.hd.serialport.listener.SerialPortMeasureListener
import com.hd.serialport.method.DeviceMeasureController
import com.hd.serialport.param.SerialPortMeasureParameter
import com.siheal.usbserialport.listener.ReceiveResultListener
import com.siheal.usbserialport.parser.DataPackageEntity
import com.siheal.usbserialport.parser.Parser
import java.io.OutputStream


/**
 * Created by hd on 2017/8/28 .
 *
 */
class SerialPortDevice(context: Context, aioDeviceType:Int, val parameter: SerialPortMeasureParameter, parser: Parser, listener: ReceiveResultListener)
    : Device(context,aioDeviceType, parser, listener), SerialPortMeasureListener {

    override fun write(outputStream: OutputStream) {
        outputStreamList.add(outputStream)
    }

    override fun measure() {
        DeviceMeasureController.measure(serialPortMeasureParameter = parameter, serialPortMeasureListener = this)
    }

    override fun measureError(message: String) {
        error(message)
    }

    override fun measuring(path: String, data: ByteArray) {
        dataQueue.put(DataPackageEntity(path = path,data = data))
    }
}