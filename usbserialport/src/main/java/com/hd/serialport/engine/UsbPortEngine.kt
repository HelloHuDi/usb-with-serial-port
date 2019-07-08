package com.hd.serialport.engine

import android.content.Context
import android.hardware.usb.UsbManager
import com.hd.serialport.R
import com.hd.serialport.help.RequestUsbPermission
import com.hd.serialport.listener.UsbMeasureListener
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.reader.UsbReadWriteRunnable
import com.hd.serialport.usb_driver.UsbSerialPort
import com.hd.serialport.utils.L

/**
 * Created by hd on 2017/8/22 .
 * usb-port engine
 */
class UsbPortEngine(context: Context, private val usbManager: UsbManager) : Engine(context) {
    
    override fun open(usbSerialPort: UsbSerialPort, parameter: UsbMeasureParameter,
                      measureListener: UsbMeasureListener) {
        super.open(usbSerialPort, parameter, measureListener)
        try {
            val usbDevice = usbSerialPort.driver.device
            if (!RequestUsbPermission.newInstance().requestUsbPermission(context, usbManager, usbDevice)) {
                L.e("request usb permission failed :$usbDevice")
            } else {
                val connection = usbManager.openDevice(usbDevice)
                if (connection != null) {
                    usbSerialPort.open(connection)
                    usbSerialPort.setParameters(parameter.baudRate, parameter.dataBits, parameter.stopBits, parameter.parity)
                    val usbReadWriteRunnable = UsbReadWriteRunnable(usbSerialPort, measureListener, this, parameter.tag)
                    submit(parameter.tag, usbReadWriteRunnable)
                    L.d("open usb device success")
                } else {
                    L.d("open device failure,connection is null")
                    measureListener.measureError(parameter.tag, context.resources.getString(R.string.open_target_device_error))
                }
            }
        } catch (ignored: Exception) {
            L.e("open device failure :$ignored")
            measureListener.measureError(parameter.tag, context.resources.getString(R.string.open_target_device_error))
        } finally {
            stop(parameter.tag)
        }
    }
}
