package com.aio.usbserialport.method

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.aio.usbserialport.R
import com.aio.usbserialport.config.AIOComponent
import com.aio.usbserialport.device.Device
import com.aio.usbserialport.device.SerialPortDevice
import com.aio.usbserialport.device.UsbPortDevice
import com.aio.usbserialport.listener.LogcatListener
import com.aio.usbserialport.listener.ReceiveResultListener
import com.aio.usbserialport.parser.EmptyParser
import com.aio.usbserialport.parser.Parser
import com.hd.serialport.config.MeasureStatus
import com.hd.serialport.method.DeviceMeasureController
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

    private var aioComponent: AIOComponent? = null

    private var status = MeasureStatus.STOPPED

    private var deviceMap = mutableMapOf<Int, Device>()

    private var parserMap = mutableMapOf<Int, Parser>()

    private var single = true

    private var t = linkedMapOf<Int, List<Any>>()

    /**
     * init measure component
     */
    fun init(context: Context, openLog: Boolean, aioComponent: AIOComponent) {
        this.aioComponent = aioComponent
        AIODeviceMeasure.context = context.applicationContext
        DeviceMeasureController.init(AIODeviceMeasure.context!!, openLog)
    }

    /**
     * single detection without logcat
     */
    fun with(aioDeviceType: Int, listener: ReceiveResultListener): AIODeviceMeasure {
        return with(aioDeviceType, listener, null)
    }

    /**
     * multiple detection without logcat
     */
    fun with(aioDeviceTypeList: List<Int>, listenerList: List<ReceiveResultListener>): AIODeviceMeasure {
        return with(aioDeviceTypeList, listenerList, null)
    }

    /**
     * single detection with logcat
     */
    fun with(aioDeviceType: Int, listener: ReceiveResultListener, logcatListener: LogcatListener?): AIODeviceMeasure {
        single = true
        initDevice(aioDeviceType, listener, logcatListener)
        return this
    }

    /**
     * multiple detection with logcat
     */
    fun with(aioDeviceTypeList: List<Int>, listenerList: List<ReceiveResultListener>, logcatListener: LogcatListener?): AIODeviceMeasure {
        single = false
        for (index in aioDeviceTypeList.indices) {
            initDevice(aioDeviceTypeList[index], if (listenerList.size == aioDeviceTypeList.size) listenerList[index] else listenerList[0], logcatListener)
        }
        return this
    }

    /**
     * add single detection condition
     */
    fun addCondition(t: List<Any>): AIODeviceMeasure {
        val par = mutableMapOf<Int, List<Any>>()
        par.put(0, t)
        return addCondition(par)
    }

    /**
     * add multiple detection condition
     */
    fun addCondition(mapT: Map<Int, List<Any>>): AIODeviceMeasure {
        mapT.forEach { AIODeviceMeasure.t.put(it.key, it.value) }
        L.d("condition : " + mapT + "=" + mapT.size + "=" + AIODeviceMeasure.t.size)
        return this
    }

    /**
     * start measure
     */
    fun startMeasure() {
        try {
            deviceMap.forEach {
                it.value.setSingle(single)
                if (single && t.isNotEmpty()) {
                    it.value.addCondition(t[0])
                } else if (t.containsKey(it.key)) {
                    it.value.addCondition(t[it.key])
                }
                it.value.startMeasure()
            }
            status = MeasureStatus.RUNNING
        } catch (ignored: Exception) {
            L.e(context!!.resources.getString(R.string.unpredictable_errors) + "=" + ignored)
            Toast.makeText(context!!, context!!.resources.getString(R.string.unpredictable_errors), Toast.LENGTH_SHORT).show()
            stopMeasure()
        }
    }

    /**
     * stop measure
     */
    fun stopMeasure() {
        L.d("stop ：+ $context+'='+$status")
        if (status == MeasureStatus.RUNNING && deviceMap.isNotEmpty()) {
            status = MeasureStatus.STOPPING
            deviceMap.forEach { it.value.stopMeasure() }
            deviceMap.clear()
            t.clear()
            status = MeasureStatus.STOPPED
        }
    }

    /**
     * query the device according to type
     */
    fun getDevice(aioDeviceType: Int): Device? {
        if (deviceMap.containsKey(aioDeviceType))
            return deviceMap[aioDeviceType]
        else return null
    }

    /**
     * query the parser according to type
     */
    fun getParser(aioDeviceType: Int): Parser? {
        if (parserMap.containsKey(aioDeviceType))
            return parserMap[aioDeviceType]
        else return null
    }

    private fun initDevice(aioDeviceType: Int, listener: ReceiveResultListener, logcatListener: LogcatListener?) {
        check(aioDeviceType)
        if (single)
            stopMeasure()
        val parser = aioComponent!!.getParser(aioDeviceType) ?: EmptyParser()
        parserMap.put(aioDeviceType, parser)
        val parameter = aioComponent!!.getMeasureParameter(context!!, aioDeviceType)
        val device: Device?
        if (parameter is SerialPortMeasureParameter) {
            device = SerialPortDevice(context!!, aioDeviceType, parameter, parser)
        } else if (parameter is UsbMeasureParameter) {
            device = UsbPortDevice(context!!, aioDeviceType, aioComponent!!.getUsbSerialPort(context!!, aioDeviceType), parameter, parser)
        } else {
            device = aioComponent!!.getOthersDevice(context!!, aioDeviceType, parser)
        }
        if (device != null) {
            device.addAIOComponent(aioComponent)
            device.addListener(listener, logcatListener)
            deviceMap.put(aioDeviceType, device)
        }
        status = MeasureStatus.PREPARE
    }

    private fun check(aioDeviceType: Int) {
        if (context == null) throw RuntimeException("please initialize context in your application")
        if (aioDeviceType < 0) throw RuntimeException("please initialize aio device type first")
        if (aioComponent == null) throw NullPointerException("aio measure component config is null !!!")
    }

}