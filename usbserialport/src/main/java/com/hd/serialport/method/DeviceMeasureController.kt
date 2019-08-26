package com.hd.serialport.method

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.serialport.SerialPortFinder
import com.hd.serialport.config.UsbPortDeviceType
import com.hd.serialport.engine.SerialPortEngine
import com.hd.serialport.engine.UsbPortEngine
import com.hd.serialport.help.RequestUsbPermission
import com.hd.serialport.help.SystemSecurity
import com.hd.serialport.listener.SerialPortMeasureListener
import com.hd.serialport.listener.UsbMeasureListener
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.usb_driver.UsbSerialDriver
import com.hd.serialport.usb_driver.UsbSerialPort
import com.hd.serialport.usb_driver.UsbSerialProber
import com.hd.serialport.utils.L
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by hd on 2017/8/22 .
 * usb device measurement controller
 */
@SuppressLint("StaticFieldLeak")
object DeviceMeasureController {
    
    private lateinit var usbManager: UsbManager
    
    private lateinit var usbPortEngine: UsbPortEngine
    
    private lateinit var serialPortEngine: SerialPortEngine
    
    fun init(context: Context, openLog: Boolean) {
        init(context, openLog, null)
    }
    
    fun init(context: Context, openLog: Boolean, callback: RequestUsbPermission.RequestPermissionCallback? = null) {
        init(context, openLog, true, callback)
    }
    
    fun init(context: Context, openLog: Boolean, requestUsbPermission: Boolean, callback: RequestUsbPermission.RequestPermissionCallback? = null) {
        if (!SystemSecurity.check(context)) throw RuntimeException("There are a error in the current system usb module !")
        L.allowLog = openLog
        this.usbManager = context.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        serialPortEngine = SerialPortEngine(context.applicationContext)
        usbPortEngine = UsbPortEngine(context.applicationContext, usbManager)
        if (requestUsbPermission)
            RequestUsbPermission.newInstance().requestAllUsbDevicePermission(context.applicationContext, callback)
    }
    
    fun scanUsbPort(): List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
    
    fun scanSerialPort(): ConcurrentHashMap<String, String> = SerialPortFinder().allDevices
    
    fun measure(usbDevice: UsbDevice, deviceType: UsbPortDeviceType, parameter: UsbMeasureParameter, listener: UsbMeasureListener) {
        val driver = UsbSerialProber.getDefaultProber().convertDriver(usbDevice,deviceType.value)
        measure(driver.ports[0], parameter, listener)
    }
    
    fun measure(usbSerialDriverList: List<UsbSerialDriver>?, parameter: UsbMeasureParameter, listener: UsbMeasureListener) {
        if (!usbSerialDriverList.isNullOrEmpty()) {
            usbSerialDriverList.filter { it.deviceType == parameter.usbPortDeviceType || parameter.usbPortDeviceType == UsbPortDeviceType.USB_OTHERS }
                    .filter { it.ports[0] != null }.forEach { measure(it.ports[0], parameter, listener) }
        } else {
            val portList = scanUsbPort()
            if (portList.isNullOrEmpty()) {
                measure(portList, parameter, listener)
            } else {
                listener.measureError(parameter.tag,"not find ports")
            }
        }
    }
    
    fun measure(usbSerialPort: UsbSerialPort?, parameter: UsbMeasureParameter, listener: UsbMeasureListener) {
        if (null != usbSerialPort) {
            usbPortEngine.open(usbSerialPort, parameter, listener)
        } else {
            measure(usbSerialDriverList = null, parameter = parameter, listener = listener)
        }
    }
    
    fun measure(paths: Array<String>?, parameter: SerialPortMeasureParameter, listeners: List<SerialPortMeasureListener>) {
        if (!paths.isNullOrEmpty()) {
            for (index in paths.indices) {
                val path = paths[index]
                when {
                    path.isNotEmpty() -> {
                        parameter.devicePath = path
                        when {
                            listeners.size == paths.size -> measure(parameter, listeners[index])
                            listeners.isNotEmpty() -> measure(parameter, listeners[0])
                            else -> L.d("not find serialPortMeasureListener")
                        }
                    }
                    index < listeners.size -> listeners[index].measureError(parameter.tag,"path is null")
                    else -> L.d("current position $index path is empty :$path ")
                }
            }
        } else {
            measure(SerialPortFinder().allDevicesPath, parameter, listeners)
        }
    }
   
    fun measure(parameter: SerialPortMeasureParameter, listener: SerialPortMeasureListener) {
        if (!parameter.devicePath.isNullOrEmpty()) {
            serialPortEngine.open(parameter, listener)
        } else {
            measure(paths = null, parameter = parameter, listeners = listOf(listener))
        }
    }
    
    /**write data by the tag filter, write all if tag==null*/
    fun write(data: List<ByteArray>?, tag: Any? = null) {
        L.d("DeviceMeasureController write usbPortEngine is working ${usbPortEngine.isWorking()}," +
                "serialPortEngine is working ${serialPortEngine.isWorking()}")
        when {
            usbPortEngine.isWorking() -> usbPortEngine.write(tag, data)
            serialPortEngine.isWorking() -> serialPortEngine.write(tag, data)
        }
    }
    
    /**stop engine by the tag filter, stop all if tag==null*/
    fun stop(tag: Any? = null) {
        L.d("DeviceMeasureController stop")
        when {
            usbPortEngine.isWorking() -> usbPortEngine.stop(tag)
            serialPortEngine.isWorking() -> serialPortEngine.stop(tag)
        }
    }
}