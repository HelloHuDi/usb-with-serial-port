package com.hd.serialport.engine

import android.content.Context
import android.hardware.usb.UsbManager
import com.hd.serialport.R
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.help.RequestUsbPermission
import com.hd.serialport.listener.UsbMeasureListener
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.reader.UsbReadWriteRunnable
import com.hd.serialport.usb_driver.UsbSerialPort
import com.hd.serialport.utils.L

/**
 * Created by hd on 2017/8/22 .
 *
 */
class UsbPortEngine(context: Context, val usbManager: UsbManager) : Engine(context) {
    override fun open(usbSerialPort: UsbSerialPort, usbMeasureParameter: UsbMeasureParameter, measureListener: UsbMeasureListener) {
        super.open(usbSerialPort, usbMeasureParameter, measureListener)
        try {
            val usbDevice = usbSerialPort.driver.device
            if (RequestUsbPermission.newInstance().requestUsbPermission(context, usbManager, usbDevice)) {
                measureListener.measureError(context.resources.getString(R.string.request_permission_failed))
                return
            }
            val connection = usbManager.openDevice(usbDevice)
            if (connection != null) {
                usbSerialPort.open(connection)
                usbSerialPort.setParameters(usbMeasureParameter.baudRate, usbMeasureParameter.dataBits,usbMeasureParameter.stopBits, usbMeasureParameter.parity)
                val usbReadWriteRunnable = UsbReadWriteRunnable(usbSerialPort, measureListener, this)
                status = MeasureStatus.RUNNING
                executor.submit(usbReadWriteRunnable)
                readWriteRunnableList.add(usbReadWriteRunnable)
            } else {
                L.d("open device failure,connection is null")
                measureListener.measureError(context.resources.getString(R.string.open_target_device_error))
            }
        } catch (ignored: Exception) {
            measureListener.measureError(context.resources.getString(R.string.open_target_device_error))
        }
    }
}
