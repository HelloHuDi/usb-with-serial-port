package com.aio.usbserialport.cache

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Base64
import com.hd.serialport.usb_driver.*
import com.hd.serialport.utils.L
import com.aio.usbserialport.utils.ParcelableUtil
import com.aio.usbserialport.utils.PreferenceUtil

/**
 * Created by hd on 2017/8/31 .
 * usb port [android.hardware.usb.UsbDevice]and
 * serial port[android.serialport.SerialPort] path
 * [com.hd.serialport.param.SerialPortMeasureParameter.devicePath]cache
 */
class UsbSerialPortCache constructor(val context: Context, val deviceType: Int) {

    companion object {
        fun newInstance(context: Context, deviceType: Int = 0) = UsbSerialPortCache(context, deviceType)
    }

    private val usbDevice_Name = "_usbDevice"

    fun getSerialPortCache() = PreferenceUtil.get(context, deviceType.toString(), "") as String

    fun getUsbDeviceCache(): UsbDevice? {
        if (!PreferenceUtil.contains(context, deviceType.toString() + usbDevice_Name)) return null
        val usbDeviceStr = PreferenceUtil.get(context, deviceType.toString() + usbDevice_Name, "") as String
        if (usbDeviceStr.isNotEmpty()) return UsbDevice.CREATOR.createFromParcel(ParcelableUtil.read(Base64.decode(usbDeviceStr, 0)))
        return null
    }

    fun getUsbPortCache(): UsbSerialPort? {
        if (!PreferenceUtil.contains(context, deviceType.toString())) return null
        val usbDevice = getUsbDeviceCache() ?: return null
        val usb_type = PreferenceUtil.get(context, deviceType.toString(), 0) as Int
        var driver: UsbSerialDriver? = null
        when (usb_type) {
        /*UsbPortDeviceType.USB_CDC_ACM */1 -> driver = CdcAcmSerialDriver(usbDevice)
        /*UsbPortDeviceType.USB_CP21xx*/ 2 -> driver = Cp21xxSerialDriver(usbDevice)
        /*UsbPortDeviceType.USB_FTD*/ 3 -> driver = FtdiSerialDriver(usbDevice)
        /*UsbPortDeviceType.USB_PL2303*/4 -> driver = ProlificSerialDriver(usbDevice)
        /*UsbPortDeviceType.USB_CH34xx*/5 -> driver = Ch34xSerialDriver(usbDevice)
        }
        if (driver != null) {
            val port = driver.ports[0]
            L.d("get cache port success")
            return port
        }
        L.d("get cache port UnSuccess")
        return null
    }

    fun removeCachePort() {
        PreferenceUtil.remove(context, deviceType.toString())
        PreferenceUtil.remove(context, deviceType.toString() + usbDevice_Name)
    }

    fun removeAllCachePort() {
        PreferenceUtil.clear(context)
    }

    fun setSerialPortCache(serialPortPath: String) {
        if (!PreferenceUtil.contains(context, deviceType.toString()))
            PreferenceUtil.put(context, deviceType.toString(), serialPortPath)
    }

    fun setUsbDeviceCache(usbDevice: UsbDevice) {
        if (!PreferenceUtil.contains(context, deviceType.toString() + usbDevice_Name))
            PreferenceUtil.put(context, deviceType.toString() + usbDevice_Name, Base64.encodeToString(ParcelableUtil.write(usbDevice), 0))
    }

    fun setUsbPortCache(usbPort: UsbSerialPort) {
        if (PreferenceUtil.contains(context, deviceType.toString())) return
        val usbDevice = usbPort.driver.device
        var usb_type = 0//UsbPortDeviceType.USB_OTHERS
        if (usbPort is CdcAcmSerialDriver.CdcAcmSerialPort) {
            usb_type = 1//UsbPortDeviceType.USB_CDC_ACM
        } else if (usbPort is Cp21xxSerialDriver.Cp21xxSerialPort) {
            usb_type = 2//UsbPortDeviceType.USB_CP21xx
        } else if (usbPort is FtdiSerialDriver.FtdiSerialPort) {
            usb_type = 3//UsbPortDeviceType.USB_FTD
        } else if (usbPort is ProlificSerialDriver.ProlificSerialPort) {
            usb_type = 4//UsbPortDeviceType.USB_PL2303
        } else if (usbPort is Ch34xSerialDriver.Ch340SerialPort) {
            usb_type = 5//UsbPortDeviceType.USB_CH34xx
        }
        PreferenceUtil.put(context, deviceType.toString(), usb_type)
        setUsbDeviceCache(usbDevice)
    }

}