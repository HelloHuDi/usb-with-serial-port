package com.hd.serialport.engine

import android.content.Context
import android.serialport.SerialPort
import com.hd.serialport.R
import com.hd.serialport.listener.SerialPortMeasureListener
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.reader.SerialPortReadWriteRunnable
import java.io.File
import java.io.IOException
import java.security.InvalidParameterException


/**
 * Created by hd on 2017/8/27 .
 * serial-port engine
 */
class SerialPortEngine(context: Context) : Engine(context) {
    
    override fun open(parameter: SerialPortMeasureParameter, measureListener: SerialPortMeasureListener) {
        super.open(parameter, measureListener)
        try {
            if (parameter.devicePath.isNullOrEmpty()) {
                measureListener.measureError(parameter.tag, context.resources.getString(R.string.error_configuration))
            } else {
                val serialPort = SerialPort(File(parameter.devicePath!!), parameter.baudRate, parameter.flags)
                val serialPortReadWriteRunnable = SerialPortReadWriteRunnable(parameter.devicePath!!, serialPort, measureListener, this, parameter.tag)
                submit(parameter.tag, serialPortReadWriteRunnable)
            }
        } catch (e: SecurityException) {
            measureListener.measureError(parameter.tag, context.resources.getString(R.string.error_security))
        } catch (e: IOException) {
            measureListener.measureError(parameter.tag, context.resources.getString(R.string.error_unknown))
        } catch (e: InvalidParameterException) {
            measureListener.measureError(parameter.tag, context.resources.getString(R.string.error_configuration))
        } finally {
            stop(parameter.tag)
        }
    }
}