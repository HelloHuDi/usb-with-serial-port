package com.aio.usbserialport.method

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.aio.usbserialport.R
import com.aio.usbserialport.config.AIOComponent
import com.aio.usbserialport.device.Device
import com.aio.usbserialport.device.SerialPortDevice
import com.aio.usbserialport.device.UsbPortDevice
import com.aio.usbserialport.listener.ReceiveResultListener
import com.aio.usbserialport.parser.EmptyParser
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.method.DeviceMeasureController
import com.hd.serialport.param.MeasureParameter
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.utils.L

/**
 * Created by hd on 2017/8/28 .
 * aio device measure controller
 */
@SuppressLint("StaticFieldLeak")
object AIODeviceMeasure {

    private var context: Context? = null

    private var aioDeviceType = -1

    private var aioComponent: AIOComponent? = null

    private var parameter: MeasureParameter? = null

    private var listener: ReceiveResultListener? = null

    private var status = MeasureStatus.STOPPED

    private var device: Device? = null

    /**
     * if you need it can set the unknown parameters
     */
    private var t: MutableList<Any> = mutableListOf()

    fun init(context: Context, openLog: Boolean, aioComponent: AIOComponent) {
        this.aioComponent = aioComponent
        AIODeviceMeasure.context = context.applicationContext
        DeviceMeasureController.init(AIODeviceMeasure.context!!, openLog)
    }

    fun with(aioDeviceType: Int, listener: ReceiveResultListener): AIODeviceMeasure {
        AIODeviceMeasure.aioDeviceType = aioDeviceType
        AIODeviceMeasure.parameter = aioComponent!!.getMeasureParameter(context!!, aioDeviceType)
        AIODeviceMeasure.listener = listener
        status = MeasureStatus.PREPARE
        return this
    }

    fun addCondition(vararg t: Any): AIODeviceMeasure {
        AIODeviceMeasure.t.clear()
        AIODeviceMeasure.t.addAll(t)
        L.d("condition : " + t + "=" + t.size + "=" + AIODeviceMeasure.t.size)
        return this
    }

    fun startMeasure() {
        try {
            check()
            status = MeasureStatus.RUNNING
            initDevice()?.startMeasure()
        } catch (ignored: Exception) {
            Toast.makeText(context!!, context!!.resources.getString(R.string.unpredictable_errors), Toast.LENGTH_SHORT).show()
            stopMeasure()
        }
    }

    fun stopMeasure() {
        L.d("stop ：+ $context+'='+$status")
        if (status == MeasureStatus.RUNNING && device != null) {
            status = MeasureStatus.STOPPING
            device!!.stopMeasure()
            device = null
            t.clear()
            status = MeasureStatus.STOPPED
        }
    }

    private fun initDevice(): Device? {
        val parser=aioComponent!!.getParser(aioDeviceType) ?: EmptyParser()
        if (parameter is SerialPortMeasureParameter) {
            device = SerialPortDevice(context!!, aioDeviceType, parameter as SerialPortMeasureParameter,parser, listener!!)
        } else if (parameter is UsbMeasureParameter) {
            device = UsbPortDevice(context!!, aioDeviceType, aioComponent!!.getUsbSerialPort(context!!, aioDeviceType),
                    parameter as UsbMeasureParameter, parser, listener!!)
        }else {
            device=aioComponent!!.getOthersDevice(context!!, aioDeviceType,parser, listener!!)
        }
        device?.addAIOComponent(aioComponent)
        device?.addCondition(t)
        return device
    }

    private fun check() {
        if (context == null) throw RuntimeException("please initialize context in your application")
        if (aioDeviceType < 0 && listener == null) throw RuntimeException("please initialize aioDeviceType first")
        stopMeasure()
    }

}