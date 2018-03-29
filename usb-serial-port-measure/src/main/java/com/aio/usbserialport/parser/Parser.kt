package com.aio.usbserialport.parser

import com.aio.usbserialport.cache.UsbSerialPortCache
import com.aio.usbserialport.device.Device
import com.aio.usbserialport.result.ParserResult
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.usb_driver.UsbSerialPort
import com.hd.serialport.utils.L
import java.nio.BufferOverflowException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by hd on 2017/8/28 .
 * parse the data from the usb port (or serial port)
 */
abstract class Parser {

    protected var readThread: Thread? = null

    protected var writeThread: Thread? = null

    protected val writeComplete = AtomicBoolean(false)

    protected var device: Device? = null

    protected var port: UsbSerialPort? = null

    protected var devicePath:String?=null

    protected val buffer = ByteBuffer.allocate(16 * 1024)!!

    fun parser(device: Device) {
        this.device = device
        if (readThread == null) readThread = Thread(Runnable { reading(device) })
        readThread!!.start()
        if (writeThread == null) writeThread = Thread(Runnable { writing(device) })
        writeThread!!.start()
    }

    private fun writing(device: Device) {
        while (device.status == MeasureStatus.RUNNING && !writeComplete.get()) {
            asyncWrite()
        }
        L.d("writing thread stop :"+device.status+"="+writeComplete.get())
    }

    private fun reading(device: Device) {
        while (device.status == MeasureStatus.RUNNING) {
            try {
                val entity = device.dataQueue.take()
                port = entity.port
                devicePath = entity.path
                buffer.put(entity.data)
                parser(entity.data)
            }catch (ignored: BufferOverflowException){
                buffer.clear()
                L.e("parser BufferOverflowException")
            }catch (e:Exception){
                L.e("parser unknown Exception:$e")
            }
        }
        L.d("reading thread stop")
    }

    fun write(byteArray: ByteArray, delay: Long = 0) {
        device?.write(byteArray, delay)
    }

    fun writeInitializationInstructAgain(delay: Long = 0) {
        device?.write(device!!.initializationInstruct(), delay)
    }

    fun error(msg: String?=null) {
        device?.error(msg)
        clear()
    }

    fun complete(result: ParserResult, stop: Boolean) {
        if (stop){
            clear()
            saveDevice()
        }
        device?.complete(result, stop)
    }

    open fun saveDevice() {
        L.d("save device :"+device!!.context+"="+device?.aioDeviceType+"="+port+"="+devicePath)
        UsbSerialPortCache.newInstance(device!!.context,device!!.aioDeviceType).setUsbSerialPortCache(usbPort=port,serialPortPath = devicePath)
    }

    private fun clear() {
        buffer.clear()
        if (!readThread!!.isAlive && !readThread!!.isInterrupted)
            readThread!!.interrupt()
        readThread = null
        if (!writeThread!!.isAlive && !writeThread!!.isInterrupted)
            writeThread!!.interrupt()
        writeThread = null
    }

    /**
     * allows asynchronous persistence parsing
     * parser complete please call [complete]
     * parser error please call [error]
     */
    abstract fun parser(data: ByteArray)

    /**
     * allow asynchronous to be written{[write] or [writeInitializationInstructAgain]} all the time
     */
    open fun asyncWrite() {}
}