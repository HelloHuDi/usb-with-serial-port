package com.hd.serialport.reader

import com.hd.serialport.engine.UsbPortEngine
import com.hd.serialport.listener.UsbMeasureListener
import com.hd.serialport.usb_driver.UsbSerialPort


/**
 * Created by hd on 2017/8/27 .
 *
 */
class UsbReadWriteRunnable(private val usbSerialPort: UsbSerialPort, measureListener: UsbMeasureListener, usbPortEngine: UsbPortEngine) :
        ReadWriteRunnable(usbPortEngine.context, measureListener) {
    init {
        measureListener.write(usbSerialPort)
    }

    override fun writing(byteArray: ByteArray) {
        usbSerialPort.write(byteArray, READ_WAIT_MILLIS * 10)
    }

    override fun reading() {
        val length = usbSerialPort.read(readBuffer.array(), READ_WAIT_MILLIS)
        if (length > 0) {
            val data = ByteArray(length)
            readBuffer.get(data, 0, length)
            (measureListener as UsbMeasureListener).measuring(usbSerialPort, data)
        }
        readBuffer.clear()
    }

    override fun close() {
        usbSerialPort.close()
    }
}