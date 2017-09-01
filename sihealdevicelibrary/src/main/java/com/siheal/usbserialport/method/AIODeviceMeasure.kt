package com.siheal.usbserialport.method

import android.annotation.SuppressLint
import android.content.Context
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.method.DeviceMeasureController
import com.hd.serialport.param.MeasureParameter
import com.hd.serialport.param.SerialPortMeasureParameter
import com.hd.serialport.param.UsbMeasureParameter
import com.hd.serialport.utils.L
import com.siheal.usbserialport.config.AIOComponent
import com.siheal.usbserialport.config.AIODeviceType
import com.siheal.usbserialport.device.Device
import com.siheal.usbserialport.device.SerialPortDevice
import com.siheal.usbserialport.device.UsbPortDevice
import com.siheal.usbserialport.listener.ReceiveResultListener
import com.siheal.usbserialport.scan.DeviceScanner

/**
 * Created by hd on 2017/8/28 .
 * aio device measure controller
 */
@SuppressLint("StaticFieldLeak")
object AIODeviceMeasure {

    private var context: Context? = null

    private var aioDeviceType = AIODeviceType.UNKNOWN_DEVICE

    private var parameter: MeasureParameter? = null

    private var listener: ReceiveResultListener? = null

    private var status = MeasureStatus.STOPPED

    private var device: Device? = null

    /**
     * if you need it can set the unknown parameters
     */
    private var t: MutableList<Any> = mutableListOf()

    fun init(context: Context, openLog: Boolean) {
        AIODeviceMeasure.context = context.applicationContext
        DeviceMeasureController.init(AIODeviceMeasure.context!!, openLog)
        status = MeasureStatus.PREPARE
    }

    fun with(aioDeviceType: Int, listener: ReceiveResultListener): AIODeviceMeasure {
        AIODeviceMeasure.aioDeviceType = aioDeviceType
        AIODeviceMeasure.parameter = AIOComponent.getMeasureParameter(context!!,aioDeviceType)
        AIODeviceMeasure.listener = listener
        return this
    }

    fun Preload() {
        check()
        DeviceScanner().scan()
    }

    fun addCondition(vararg t: Any): AIODeviceMeasure {
        AIODeviceMeasure.t.clear()
        AIODeviceMeasure.t.addAll(t)
        L.d("condition : " + t + "=" + t.size + "=" + AIODeviceMeasure.t.size)
        return this
    }

    fun startMeasure() {
        check()
        status = MeasureStatus.RUNNING
        initDevice()?.startMeasure()
    }

    fun stopMeasure() {
        L.d("stop ：+ $context+'='+$status")
        if (status == MeasureStatus.RUNNING && device != null) {
            status = MeasureStatus.STOPPING
            device!!.stopMeasure()
            device=null
            t.clear()
            status = MeasureStatus.STOPPED
        }
    }

    private fun initDevice(): Device? {
        if (parameter is SerialPortMeasureParameter) {
            device = SerialPortDevice(context!!, aioDeviceType, parameter as SerialPortMeasureParameter,
                    AIOComponent.getParser(aioDeviceType)!!, listener!!)
        } else if (parameter is UsbMeasureParameter) {
            device=UsbPortDevice(context!!, aioDeviceType, AIOComponent.getUsbSerialPort(context!!, aioDeviceType),
                    parameter as UsbMeasureParameter, AIOComponent.getParser(aioDeviceType)!!, listener!!)
        }
        device?.addCondition(t)
        return device
    }

    private fun check() {
        if (context == null) throw RuntimeException("please initialize context in your application")
        if (aioDeviceType < 0 && listener == null) throw RuntimeException("please initialize aioDeviceType first")
        stopMeasure()
    }

}