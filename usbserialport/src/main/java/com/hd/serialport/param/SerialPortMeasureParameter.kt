package com.hd.serialport.param


/**
 * Created by hd on 2017/8/27 .
 * @param devicePath serial port path [android.serialport.SerialPortFinder.getAllDevicesPath]
 * @param baudRate baud rate as an integer, for example {@code 115200}.
 * @param flags  default value :0
 * @param tag set tag
 */
data class SerialPortMeasureParameter(var devicePath: String? = null, var baudRate: Int = 115200,
                                      var flags: Int = 0, var tag :Any?="default_serial_tag"):MeasureParameter()