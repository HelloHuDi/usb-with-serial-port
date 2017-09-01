package com.hd.serialport.usb_driver;

import android.hardware.usb.UsbDevice;
import android.support.annotation.Keep;

import java.util.Collections;
import java.util.List;

/**
 * Created by hd on 2017/2/27 0027.
 */
@Keep
abstract class CommonUsbSerialDriver implements UsbSerialDriver {
    UsbDevice mDevice;
    UsbSerialPort mPort;

    @Override
    public UsbDevice getDevice() {
        return mDevice;
    }

    @Override
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(mPort);
    }
}
