package com.hd.serialport.reader

import android.content.Context
import com.hd.serialport.R
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.listener.MeasureListener
import com.hd.serialport.utils.L
import java.nio.ByteBuffer


/**
 * Created by hd on 2017/8/27 .
 *
 */
abstract class ReadWriteRunnable(val context: Context, val measureListener: MeasureListener) : Runnable {

    private val MAX_BUFFER_SIZE = 16 * 4096

    protected val READ_WAIT_MILLIS = 100

    protected var status=MeasureStatus.PREPARE

    protected val readBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE)!!

    private val writeBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE)!!

    init {
        status=MeasureStatus.RUNNING
    }

    /**
     * allow write data into port,it`s recommended async write to use
     * [com.hd.serialport.listener.SerialPortMeasureListener.write] or
     * [com.hd.serialport.listener.UsbMeasureListener.write]
     * at write large data volumes
     **/
    fun write(data: ByteArray) {
        synchronized(writeBuffer) {
            writeBuffer.put(data)
        }
    }

    fun stop() {
        L.d("read-write runnable stop 1 :$status")
        if (status != MeasureStatus.STOPPED || status != MeasureStatus.STOPPING) {
            status = MeasureStatus.STOPPING
            readBuffer.clear()
            writeBuffer.clear()
            close()
            status = MeasureStatus.STOPPED
            L.d("read-write runnable stop 2 :$status")
        }
    }

    abstract fun writing(byteArray: ByteArray)

    abstract fun reading()

    abstract fun close()

    override fun run() {
        while (status != MeasureStatus.STOPPED) {
            try {
                reading()
            } catch (ignored: Exception) {
                L.d("reading into port error :$ignored")
                measureListener.measureError(context.resources.getString(R.string.measure_target_device_error))
                try {
                    close()
                } catch (ignored: Exception) {
                    L.d("close error :$ignored")
                }
                status = MeasureStatus.STOPPED
                break
            }
            try {
                writing()
            } catch (ignored: Exception) {
                L.d("writing into port error")
            }
        }
    }

    private fun writing() {
        synchronized(writeBuffer) {
            val len = writeBuffer.position()
            if (len > 0) {
                val outBuff = ByteArray(len)
                writeBuffer.rewind()
                writeBuffer.get(outBuff, 0, len)
                writeBuffer.clear()
                writing(outBuff)
            }
        }
    }

}