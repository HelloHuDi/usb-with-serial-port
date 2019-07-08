package com.aio.usbserialport.cache

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Base64
import com.aio.usbserialport.utils.ParcelableUtil
import com.aio.usbserialport.utils.PreferenceUtil
import com.hd.serialport.usb_driver.*
import com.hd.serialport.utils.L

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
    private val usbPort_Name = "_usbPort"
    private val serialPort_Name = "_serialPort"

    fun getSerialPortCache() = PreferenceUtil.get(context, deviceType.toString()+serialPort_Name, "") as String

    fun getUsbDeviceCache(): UsbDevice? {
        if (!PreferenceUtil.contains(context, deviceType.toString() + usbDevice_Name)) return null
        val usbDeviceStr = PreferenceUtil.get(context, deviceType.toString() + usbDevice_Name, "") as String
        if (usbDeviceStr.isNotEmpty())
            return UsbDevice.CREATOR.createFromParcel(ParcelableUtil.read(Base64.decode(usbDeviceStr, 0)))
        return null
    }

    fun getUsbPortCache(): UsbSerialPort? {
        if (!PreferenceUtil.contains(context, deviceType.toString()+usbPort_Name)) return null
        val usbDevice = getUsbDeviceCache() ?: return null
        val usb_type = PreferenceUtil.get(context, deviceType.toString()+usbPort_Name, 0) as Int
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
            L.d("get cache port success:"+driver+"="+driver.deviceType)
            return port
        }
        L.d("get cache port UnSuccess")
        return null
    }

    fun removeCachePort() {
        PreferenceUtil.remove(context, deviceType.toString()+usbPort_Name)
        PreferenceUtil.remove(context, deviceType.toString()+serialPort_Name)
        PreferenceUtil.remove(context, deviceType.toString() + usbDevice_Name)
    }

    fun removeAllCachePort() {
        PreferenceUtil.clear(context)
    }

    fun setUsbSerialPortCache(usbPort: UsbSerialPort?=null,usbDevice: UsbDevice?=null,serialPortPath: String?=null){
        if(usbPort!=null){
            setUsbPortCache(usbPort)
        }else if(usbDevice!=null){
            setUsbDeviceCache(usbDevice)
        }else if(!serialPortPath.isNullOrEmpty()){
            setSerialPortCache(serialPortPath)
        }
    }

    fun setSerialPortCache(serialPortPath: String) {
        if (!PreferenceUtil.contains(context, deviceType.toString()+serialPort_Name))
            PreferenceUtil.put(context, deviceType.toString()+serialPort_Name, serialPortPath)
    }

    fun setUsbDeviceCache(usbDevice: UsbDevice) {
        if (!PreferenceUtil.contains(context, deviceType.toString() + usbDevice_Name))
            PreferenceUtil.put(context, deviceType.toString() + usbDevice_Name, Base64.encodeToString(ParcelableUtil.write(usbDevice), 0))
    }

    fun setUsbPortCache(usbPort: UsbSerialPort) {
        val usbDevice = usbPort.driver.device
        var usb_type = 0//UsbPortDeviceType.USB_OTHERS
        when (usbPort) {
            is CdcAcmSerialDriver.CdcAcmSerialPort -> usb_type = 1//UsbPortDeviceType.USB_CDC_ACM
            is Cp21xxSerialDriver.Cp21xxSerialPort -> usb_type = 2//UsbPortDeviceType.USB_CP21xx
            is FtdiSerialDriver.FtdiSerialPort -> usb_type = 3//UsbPortDeviceType.USB_FTD
            is ProlificSerialDriver.ProlificSerialPort -> usb_type = 4//UsbPortDeviceType.USB_PL2303
            is Ch34xSerialDriver.Ch340SerialPort -> usb_type = 5//UsbPortDeviceType.USB_CH34xx
        }
        PreferenceUtil.put(context, deviceType.toString()+usbPort_Name, usb_type)
        setUsbDeviceCache(usbDevice)
    }

}