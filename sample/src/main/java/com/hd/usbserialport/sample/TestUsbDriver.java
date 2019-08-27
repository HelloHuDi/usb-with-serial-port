package com.hd.usbserialport.sample;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.support.annotation.NonNull;

import com.hd.serialport.usb_driver.CommonUsbSerialDriver;
import com.hd.serialport.usb_driver.CommonUsbSerialPort;
import com.hd.serialport.usb_driver.UsbSerialDriver;
import com.hd.serialport.usb_driver.UsbSerialPort;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by hd on 2019-08-26 .
 */
public class TestUsbDriver extends CommonUsbSerialDriver {

    public TestUsbDriver(UsbDevice mDevice) {
        super(mDevice);
    }

    @Override
    public UsbSerialPort setPort(UsbDevice mDevice) {
        return new TestUsbPort(mDevice, 0);
    }

    @NonNull
    @Override
    public String setDriverName() {
        return "test";
    }

    public class TestUsbPort extends CommonUsbSerialPort{

        public TestUsbPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
        }

        @Override
        public UsbSerialDriver getDriver() {
            return null;
        }

        @Override
        public void open(UsbDeviceConnection connection) throws IOException {

        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            return 0;
        }

        @Override
        public int write(byte[] src, int timeoutMillis) throws IOException {
            return 0;
        }

        @Override
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {

        }

        @Override
        public boolean getCD() throws IOException {
            return false;
        }

        @Override
        public boolean getCTS() throws IOException {
            return false;
        }

        @Override
        public boolean getDSR() throws IOException {
            return false;
        }

        @Override
        public boolean getDTR() throws IOException {
            return false;
        }

        @Override
        public void setDTR(boolean value) throws IOException {

        }

        @Override
        public boolean getRI() throws IOException {
            return false;
        }

        @Override
        public boolean getRTS() throws IOException {
            return false;
        }

        @Override
        public void setRTS(boolean value) throws IOException {

        }

    }

    //本方法名必须这样写，否则报错
    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
        //add vendor and  product
//        supportedDevices.put(Integer.valueOf(UsbId.VENDOR_ARDUINO), new int[]{UsbId.ARDUINO_UNO, UsbId.ARDUINO_UNO_R3,
//                                                                              UsbId.ARDUINO_MEGA_2560, UsbId.ARDUINO_MEGA_2560_R3,
//                                                                              UsbId.ARDUINO_SERIAL_ADAPTER, UsbId.ARDUINO_SERIAL_ADAPTER_R3,
//                                                                              UsbId.ARDUINO_MEGA_ADK, UsbId.ARDUINO_MEGA_ADK_R3,
//                                                                              UsbId.ARDUINO_LEONARDO, UsbId.ARDUINO_MICRO,});
        return supportedDevices;
    }
}
