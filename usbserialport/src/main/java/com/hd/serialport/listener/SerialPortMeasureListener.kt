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
     */
    fun measuring(path:String,data: ByteArray)

    /**
     * only initialize one time,write data into serial port [OutputStream.write]
     */
    fun write(outputStream: OutputStream)

}