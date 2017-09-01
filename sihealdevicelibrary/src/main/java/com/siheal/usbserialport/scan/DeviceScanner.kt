package com.siheal.usbserialport.scan

import com.hd.serialport.method.DeviceMeasureController
import com.hd.serialport.usb_driver.UsbSerialDriver
import com.hd.serialport.usb_driver.UsbSerialPort
import com.hd.serialport.utils.L
import java.util.concurrent.ConcurrentHashMap


/**
 * Created by hd on 2017/8/28 .
 * scan aio device
 */
class DeviceScanner {

    val allUsbSerialDriver: List<UsbSerialDriver> by lazy { DeviceMeasureController.scanUsbPort() }

    val allSerialPort: ConcurrentHashMap<String, String> by lazy { DeviceMeasureController.scanSerialPort() }

    fun scan() {
        val usbSerialPortList= arrayListOf<UsbSerialPort>()
        allUsbSerialDriver.map { it.ports }.forEach { ports ->
            ports.indices.map { ports[it] }.filterNot { classifyDeviceByCondition(it) }.forEach { usbSerialPortList.add(it) }
        }
        if(usbSerialPortList.isNotEmpty()){
            L.d("need others handle size :" + usbSerialPortList.size)
//            ClassifyUsbPort(usbSerialPortList).scan()
        }
        //select serial port device
        //AIO serial port device is fixed
//        ClassifySerialPort(allSerialPort).scan()
    }

    private fun classifyDeviceByCondition(usbSerialPort: UsbSerialPort): Boolean {
        if (!classifyDeviceByUsbID(usbSerialPort)) {
             if(classifyDeviceByUsbType(usbSerialPort)){
                 TODO("保存")
                 return true
             }
             return false
        }
        TODO("保存")
        return true
    }

    private fun classifyDeviceByUsbID(usbSerialPort: UsbSerialPort): Boolean {
//        val usbDevice = usbSerialPort.driver.device
//        return usbDevice.productId == 33485 && usbDevice.vendorId == 4292//select body fat usb device
        return false
    }

    private fun classifyDeviceByUsbType(usbSerialPort: UsbSerialPort): Boolean {
        return false
    }
}