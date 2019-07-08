package com.hd.serialport.engine

import android.content.Context
import android.os.SystemClock
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
import java.util.concurrent.atomic.AtomicInteger


/**
 * Created by hd on 2017/8/27 .
 * engine
 */
abstract class Engine(val context: Context) {
    
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    
    private val defaultKey = AtomicInteger(-1)
    
    private val defaultTagPrefix = "engine_"
    
    private val runnableMap = mutableMapOf<String, ReadWriteRunnable>()
    
    private val tagMap = mutableMapOf<String, Any>()
    
    private val statusMap = mutableMapOf<String, MeasureStatus>()
    
    private var status: MeasureStatus = MeasureStatus.PREPARE
    
    fun submit(tag: Any?, runnable: ReadWriteRunnable) {
        status = MeasureStatus.RUNNING
        defaultKey.incrementAndGet()
        val customTag = defaultTagPrefix + defaultKey.get().toString()
        runnableMap[customTag] = runnable
        tagMap[customTag] = tag ?: defaultTagPrefix
        statusMap[customTag] = status
        executor.submit(runnable)
    }
    
    fun write(tag: Any? = null, data: List<ByteArray>?) {
        try {
            if (!data.isNullOrEmpty() && isWorking()) {
                tagMap.filter { tag == null || tag.toString().isEmpty() || it.value == tag }.map { runnableMap[it.key] }.forEach { runnable ->
                    data.forEach {
                        runnable?.write(it)
                        SystemClock.sleep(10)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stop(tag: Any? = null) {
        L.d("Engine executor stop ?" + status + ",runnable size :" + runnableMap.size)
        try {
            if (!isWorking()) {
                tagMap.filter { tag == null || tag.toString().isEmpty() || it.value == tag }.map {
                    statusMap[it.key] = MeasureStatus.STOPPED
                    runnableMap[it.key]
                }.forEach {
                    it?.stop()
                    SystemClock.sleep(10)
                }
                status = if (statusMap.values.contains(MeasureStatus.RUNNING)) MeasureStatus.RUNNING else MeasureStatus.STOPPED
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            release()
        }
    }
    
    fun isWorking(): Boolean = status == MeasureStatus.RUNNING
    
    private fun release() {
        if (!isWorking()) {
            tagMap.clear()
            statusMap.clear()
            runnableMap.clear()
            defaultKey.set(-1)
            status = MeasureStatus.PREPARE
        }
    }
    
    open fun open(usbSerialPort: UsbSerialPort, parameter: UsbMeasureParameter, measureListener: UsbMeasureListener) {}
    
    open fun open(parameter: SerialPortMeasureParameter, measureListener: SerialPortMeasureListener) {}
    
}
