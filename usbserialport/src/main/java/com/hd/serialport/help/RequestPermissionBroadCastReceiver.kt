package com.hd.serialport.help

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hd.serialport.utils.L

/**
 * Created by hd on 2017/8/22.
 * request permission at midway equipment access
 */
class RequestPermissionBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                L.d("Note: midway equipment access ===> $usbDevice")
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED ->  L.d("Note: there is a device to be removed!")
        }
    }
}
