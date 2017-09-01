package com.hd.serialport.engine

import android.content.Context
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.listener.SerialPortMeasureListener
import com.hd.serialport.listener.UsbMeasureListener
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.reader.ReadWriteRunnable
import com.hd.serialport.usb_driver.UsbSerialPort
import com.hd.serialport.utils.L
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Created by hd on 2017/8/27 .
 *
 */
abstract class Engine(val context: Context) {

    var status: MeasureStatus = MeasureStatus.PREPARE

    val executor: ExecutorService = Executors.newCachedThreadPool()

    val readWriteRunnableList = arrayListOf<ReadWriteRunnable>()

    fun write(data: List<ByteArray>?) {
        if (data != null && data.isNotEmpty())
            readWriteRunnableList.forEach { readWriteRunnable ->
                data.indices.map { data[it] }.filter { status == MeasureStatus.RUNNING }.forEach { readWriteRunnable.write(it) }
            }
    }

    fun stop() {
        status = MeasureStatus.STOPPED
        L.d("Engine executor stop ?" + executor.isShutdown+"=" + status + "=" + readWriteRunnableList.size)
        readWriteRunnableList.forEach { it.stop() }
        executor.shutdownNow()
        readWriteRunnableList.clear()
    }

    open fun open(usbSerialPort: UsbSerialPort, usbMeasureParameter: UsbMeasureParameter, measureListener: UsbMeasureListener) {}

    open fun open(parameter: SerialPortMeasureParameter, measureListener: SerialPortMeasureListener) {}

}
