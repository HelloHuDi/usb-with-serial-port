package com.siheal.usbserialport.parser

import com.hd.serialport.usb_driver.UsbSerialPort


/**
 * Created by hd on 2017/8/28 .
 * @param port usb port [UsbSerialPort]
 * @param path serial port device path[com.hd.serialport.param.SerialPortMeasureParameter.devicePath]
 * @param data receive data from port[UsbSerialPort]or[android.serialport.SerialPort]
 */
data class DataPackageEntity (var port:UsbSerialPort?=null,var path:String?=null, var data: ByteArray)