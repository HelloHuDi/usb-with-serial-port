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
import com.hd.serialport.usb_driver.*
import com.hd.serialport.utils.L
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by hd on 2017/8/22 .
 * usb device measurement controller
 */
@SuppressLint("StaticFieldLeak")
object DeviceMeasureController {

    private var usbManager: UsbManager? = null

    private var usbPortEngine: UsbPortEngine? = null

    private var serialPortEngine: SerialPortEngine? = null

    private var usbPortMeasure = false

    private var serialPortMeasure = false

    fun init(context: Context, openLog: Boolean) {
        init(context, openLog, null)
    }

    fun init(context: Context, openLog: Boolean, callback: RequestUsbPermission.RequestPermissionCallback? = null) {
        if (!SystemSecurity.check(context)) throw RuntimeException("There are a error in the current system usb module !")
        L.allowLog = openLog
        usbPortMeasure = false
        serialPortMeasure = false
        this.usbManager = context.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        serialPortEngine = SerialPortEngine(context.applicationContext)
        usbPortEngine = UsbPortEngine(context.applicationContext, usbManager!!)
        RequestUsbPermission.newInstance().requestAllUsbDevicePermission(context.applicationContext, callback)
    }

    fun scanUsbPort(): List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

    fun scanSerialPort(): ConcurrentHashMap<String, String> = SerialPortFinder().allDevices

    fun measure(usbDevice: UsbDevice, deviceType: UsbPortDeviceType, usbMeasureParameter: UsbMeasureParameter, usbMeasureListener: UsbMeasureListener) {
        val driver: UsbSerialDriver
        when (deviceType) {
            UsbPortDeviceType.USB_CDC_ACM -> driver = CdcAcmSerialDriver(usbDevice)
            UsbPortDeviceType.USB_CP21xx -> driver = Cp21xxSerialDriver(usbDevice)
            UsbPortDeviceType.USB_FTD -> driver = FtdiSerialDriver(usbDevice)
            UsbPortDeviceType.USB_PL2303 -> driver = ProlificSerialDriver(usbDevice)
            UsbPortDeviceType.USB_CH34xx -> driver = Ch34xSerialDriver(usbDevice)
            else -> throw NullPointerException("unknown usb device type:" + deviceType)
        }
        measure(driver.ports[0], usbMeasureParameter, usbMeasureListener)
    }

    fun measure(usbSerialDriverList: List<UsbSerialDriver>?, usbMeasureParameter: UsbMeasureParameter, usbMeasureListener: UsbMeasureListener) {
        if (usbSerialDriverList != null) {
            usbSerialDriverList.filter { it.deviceType == usbMeasureParameter.usbPortDeviceType || usbMeasureParameter.usbPortDeviceType==UsbPortDeviceType.USB_OTHERS }
                    .filter { it.ports[0] != null }.forEach { measure(it.ports[0], usbMeasureParameter, usbMeasureListener) }
        } else {
            measure(scanUsbPort(), usbMeasureParameter, usbMeasureListener)
        }
    }

    fun measure(usbSerialPort: UsbSerialPort?, usbMeasureParameter: UsbMeasureParameter, usbMeasureListener: UsbMeasureListener) {
        if (usbSerialPort != null) {
            usbPortEngine?.open(usbSerialPort, usbMeasureParameter, usbMeasureListener)
            usbPortMeasure = true
        } else {
            measure(usbSerialDriverList = null, usbMeasureParameter = usbMeasureParameter, usbMeasureListener = usbMeasureListener)
        }
    }

    fun measure(paths: Array<String>?, serialPortMeasureParameter: SerialPortMeasureParameter, serialPortMeasureListener: SerialPortMeasureListener) {
        if (paths != null) {
            for (path in paths) {
                if (!path.isNullOrEmpty()) {
                    serialPortMeasureParameter.devicePath = path
                    measure(serialPortMeasureParameter, serialPortMeasureListener)
                }
            }
        } else {
            measure(SerialPortFinder().allDevicesPath, serialPortMeasureParameter, serialPortMeasureListener)
        }
    }

    fun measure(serialPortMeasureParameter: SerialPortMeasureParameter, serialPortMeasureListener: SerialPortMeasureListener) {
        if (!serialPortMeasureParameter.devicePath.isNullOrEmpty()) {
            serialPortEngine?.open(serialPortMeasureParameter, serialPortMeasureListener)
            serialPortMeasure = true
        } else {
            measure(paths = null, serialPortMeasureParameter = serialPortMeasureParameter, serialPortMeasureListener = serialPortMeasureListener)
        }
    }

    fun write(data: List<ByteArray>?) {
        if (usbPortMeasure)
            usbPortEngine?.write(data)
        if (serialPortMeasure)
            serialPortEngine?.write(data)
    }

    fun stop() {
        L.d("DeviceMeasureController stop")
        if (usbPortMeasure) {
            usbPortEngine?.stop()
            usbPortMeasure = false
        }
        if (serialPortMeasure) {
            serialPortEngine?.stop()
            serialPortMeasure = false
        }
    }
}