package com.hd.serialport.reader

import android.serialport.SerialPort
import com.hd.serialport.engine.SerialPortEngine
import com.hd.serialport.listener.SerialPortMeasureListener


/**
 * Created by hd on 2017/8/27 .
 *
 */
class SerialPortReadWriteRunnable(private val devicePath: String, private val serialPort: SerialPort,
                                  listener: SerialPortMeasureListener, engine: SerialPortEngine, tag: Any?) :
        ReadWriteRunnable(tag, engine.context, listener) {
    init {
        (measureListener as SerialPortMeasureListener).write(tag, serialPort.outputStream!!)
    }
    
    override fun writing(byteArray: ByteArray) {
        serialPort.outputStream?.write(byteArray)
    }
    
    override fun reading() {
        var length = serialPort.inputStream?.available() ?: 0
        if (length > 0) {
            length = serialPort.inputStream!!.read(readBuffer.array())
            val data = ByteArray(length)
            readBuffer.get(data, 0, length)
            (measureListener as SerialPortMeasureListener).measuring(tag, devicePath, data)
            readBuffer.clear()
        }
    }
    
    override fun close() {
        try {
            serialPort.outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            serialPort.inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
}