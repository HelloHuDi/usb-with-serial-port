package com.hd.serialport.help

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.AsyncTask
import com.hd.serialport.utils.L
import java.util.*

/**
 * Created by hd on 2017/8/22.
 * request the usb permissions at the terminal device.
 */
class RequestUsbPermission {
    private val usb_permission = "com.hd.usbHost.device.USB_PERMISSION"
    private var usbDeviceList: MutableList<UsbDevice>? = null
    private var position = 0
    private var usbManager: UsbManager? = null
    private var callback: RequestPermissionCallback? = null

    interface RequestPermissionCallback {

        /**
         * all usb permission request complete
         */
        fun complete()

        /**
         * may not have device
         */
        fun failed()
    }

    fun requestUsbPermission(context: Context, usbManager: UsbManager?, device: UsbDevice?): Boolean {
        this.usbManager = usbManager
        if (device == null)
            return false
        if (this.usbManager == null)
            this.usbManager = getUsbManager(context)
        if (!this.usbManager!!.hasPermission(device)) {
            this.usbManager!!.requestPermission(device, getPendingIntent(context, device))
            return this.usbManager!!.hasPermission(device)
        }
        return true
    }

    fun requestAllUsbDevicePermission(context: Context, callback: RequestPermissionCallback?=null) {
            this.callback = callback
            requestAllUsbDevicePermission(context)
    }

    private fun requestAllUsbDevicePermission(context: Context) {
        asyncRequestAllUsbDevicePermission(context)
    }

    @SuppressLint("StaticFieldLeak")
    private fun asyncRequestAllUsbDevicePermission(context: Context) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                RootCmd.execRootCmdOrder()//execute root order
                val usbManager = getUsbManager(context)
                usbDeviceList = LinkedList<UsbDevice>()
                usbDeviceList!!.addAll(usbManager.deviceList.values)
                position = 0
                if (usbDeviceList!!.size > 0) {
                    request(context, usbManager, usbDeviceList!![position])
                } else {
                    callback?.failed()
                }
                return null
            }
        }.execute()
    }

    private fun getUsbManager(context: Context): UsbManager {
        return context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    private fun getPendingIntent(context: Context, device: UsbDevice?): PendingIntent {
        return PendingIntent.getBroadcast(context.applicationContext,
                device?.hashCode() ?: 0, Intent(usb_permission), PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun request(context: Context, usbManager: UsbManager, usbDevice: UsbDevice) {
        if (!usbManager.hasPermission(usbDevice)) {
            intent = context.registerReceiver(usbBroadCastReceiver, IntentFilter(usb_permission))
            usbManager.requestPermission(usbDevice, getPendingIntent(context, usbDevice))
        } else {
            L.d("current usb device has permission")
            requestContinue(context, usbManager)
        }
    }

    private var intent: Intent? = null

    private fun requestContinue(context: Context, usbManager: UsbManager) {
        position++
        if (position < usbDeviceList!!.size) {
            request(context, usbManager, usbDeviceList!![position])
        } else {
            if (intent != null) {
                usbBroadCastReceiver.abortBroadcast()
                context.unregisterReceiver(usbBroadCastReceiver)
            }
            callback?.complete()
        }
    }

    private val usbBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                usb_permission -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val success = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    L.d("权限响应：" + device.hashCode() + "===" + success + "===" + device)
                    requestContinue(context, getUsbManager(context))
                }
            }
        }
    }

    companion object {

        fun newInstance(): RequestUsbPermission {
            return RequestUsbPermission()
        }
    }
}
