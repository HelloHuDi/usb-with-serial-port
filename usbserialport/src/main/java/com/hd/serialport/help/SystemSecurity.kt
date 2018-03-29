package com.hd.serialport.help

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hd.serialport.utils.L

/**
 * Created by hd on 2017/8/22.
 *
 * some vendors provide SDK maybe exists error in the usb module.
 *
 * the error come from the API [UsbDevice.getInterface] and belongs to the system SDK error,
 * might throw a error [NullPointerException] after when you traverse all usb and call it.
 *
 * this error need the vendors to modify the underlying code,
 * however,in fact, we can also go to avoid it by use API[UsbDevice.getInterfaceCount] to judge,
 * but，in order to avoid other unknown problems to modify the underlying code is the best way.
 *
 * in theory, you only need to check once,so ,advice used that in project debugging or test project.
 */
object SystemSecurity {

    /**
     * @return return true if the current system security's support usb devices
     */
    fun check(context: Context): Boolean {
        return try {
            val usbManager = context.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList = usbManager.deviceList
            for (usbDevice in deviceList.values)
                usbDevice.getInterface(0)
            true
        } catch (e: Exception) {
            L.d("TAG", "There are errors in the current system usb module :$e")
            false
        }
    }
}
