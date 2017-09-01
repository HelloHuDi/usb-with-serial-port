package com.hd.serialport.help

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Toast
import com.hd.serialport.utils.L

/**
 * Created by hd on 2017/8/22.
 * request permission at midway equipment access
 */
class RequestPermissionBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                ToastMore(context,"注意：有设备接入!")
                val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                L.d("midway equipment access :" + usbDevice)
                RequestUsbPermission.newInstance().requestUsbPermission(context, null, usbDevice)
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> ToastMore(context,"注意：有设备移除!")
        }
    }

    fun ToastMore(context:Context,str:String){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show()
    }
}
