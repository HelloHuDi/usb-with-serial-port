package com.hd.serialport.listener

import com.hd.serialport.usb_driver.UsbSerialPort

/**
 * Created by hd on 2017/8/27 .
 
 */
interface UsbMeasureListener : MeasureListener {
    
    /**
     * receive data[data] from usb port[usbSerialPort]
     */
    fun measuring(tag: Any?, usbSerialPort: UsbSerialPort, data: ByteArray)
    
    /**
     * only initialize one time,write data into usb port [UsbSerialPort.write]
     */
    fun write(tag: Any?, usbSerialPort: UsbSerialPort)
}
