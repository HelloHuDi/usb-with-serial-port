package com.hd.serialport.param

import com.hd.serialport.config.UsbPortDeviceType


/**
 * Created by hd on 2017/8/22 .
 * Sets various serial port parameters.
 * @param usbPortDeviceType set usb type [UsbPortDeviceType]
 * @param baudRate baud rate as an integer, for example {@code 115200}.
 * @param dataBits one of {@link UsbSerialPort#DATABITS_5}, {@link UsbSerialPort#DATABITS_6},
 * {@link UsbSerialPort#DATABITS_7}, or {@link UsbSerialPort#DATABITS_8}.
 * @param stopBits one of {@link UsbSerialPort#STOPBITS_1}, {@link UsbSerialPort#STOPBITS_1_5}, or
 * {@link UsbSerialPort#STOPBITS_2}.
 * @param parity one of {@link UsbSerialPort#PARITY_NONE}, {@link UsbSerialPort#PARITY_ODD},
 * {@link UsbSerialPort#PARITY_EVEN}, {@link UsbSerialPort#PARITY_MARK}, or
 * {@link UsbSerialPort#PARITY_SPACE}.
 * @param tag set tag
 */
data class UsbMeasureParameter(var usbPortDeviceType: UsbPortDeviceType?=UsbPortDeviceType.USB_OTHERS,
                               var baudRate: Int = 115200, var dataBits: Int = 8, var stopBits: Int = 1,
                               var parity: Int = 0,var tag : Any?="default_usb_tag"):MeasureParameter()