package com.hd.serialport.usb_driver;

import android.hardware.usb.UsbDevice;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.hd.serialport.config.UsbPortDeviceType;

import java.util.Collections;
import java.util.List;

/**
 * Created by hd on 2017/2/27 0027.
 */
@Keep
abstract class CommonUsbSerialDriver implements UsbSerialDriver {

    public UsbDevice mDevice;

    public UsbSerialPort mPort;

    public abstract UsbSerialPort setPort(UsbDevice mDevice);

    @NonNull
    public abstract String setDriverName();

    public CommonUsbSerialDriver(UsbDevice mDevice) {
        this.mDevice = mDevice;
        mPort = setPort(mDevice);
    }

    @Override
    public UsbDevice getDevice() {
        return mDevice;
    }

    @Override
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(mPort);
    }

    @Override
    public UsbPortDeviceType getDeviceType() {
        UsbPortDeviceType type = UsbPortDeviceType.USB_CUSTOM_TYPE;
        type.setValue(setDriverName());
        return type;
    }
}
