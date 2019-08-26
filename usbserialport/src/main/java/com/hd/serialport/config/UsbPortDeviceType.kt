package com.hd.serialport.config


/**
 * Created by hd on 2017/8/22 .
 * support device type
 */
enum class UsbPortDeviceType(var value: String? = DriversType.USB_PL2303) {
    USB_OTHERS,
    /**custom driver type*/
    USB_CUSTOM_TYPE,
}

object DriversType {
    const val USB_CP21xx = "usb_cp21xx"
    const val USB_PL2303 = "usb_pl2303"
    const val USB_FTD = "usb_ftd"
    const val USB_CDC_ACM = "usb_cdc_acm"
    const val USB_CH34xx = "usb_ch34xx"
}