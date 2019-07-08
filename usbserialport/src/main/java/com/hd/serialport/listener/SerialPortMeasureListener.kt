package com.hd.serialport.listener

import java.io.OutputStream


/**
 * Created by hd on 2017/8/27 .
 *
 */
interface SerialPortMeasureListener : MeasureListener {
    /**
     * receive data from serial port
     * @param path serial port device path[com.hd.serialport.param.SerialPortMeasureParameter.devicePath]
     * @param tag [com.hd.serialport.param.SerialPortMeasureParameter.tag]
     */
    fun measuring(tag: Any?, path: String, data: ByteArray)
    
    /**
     * only initialize one time,write data into serial port [OutputStream.write]
     * @param tag [com.hd.serialport.param.SerialPortMeasureParameter.tag]
     */
    fun write(tag: Any?, outputStream: OutputStream)
    
}