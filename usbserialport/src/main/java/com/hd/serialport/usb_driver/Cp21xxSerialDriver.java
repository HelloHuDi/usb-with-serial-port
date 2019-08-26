/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.hd.serialport.usb_driver;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.hd.serialport.config.DriversType;
import com.hd.serialport.utils.L;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Keep
public class Cp21xxSerialDriver extends CommonUsbSerialDriver {

    private static final String TAG = Cp21xxSerialDriver.class.getSimpleName();

    public Cp21xxSerialDriver(UsbDevice mDevice) {
        super(mDevice);
    }

    @Override
    public UsbSerialPort setPort(UsbDevice mDevice) {
        return new Cp21xxSerialPort(mDevice, 0);
    }

    @NonNull
    @Override
    public String setDriverName() {
        return DriversType.USB_CP21xx;
    }

    public class Cp21xxSerialPort extends CommonUsbSerialPort {

        private static final int DEFAULT_BAUD_RATE = 9600;

        private static final int USB_WRITE_TIMEOUT_MILLIS = 5000;
        /**
         * Configuration Request Types
         */
        private static final int REQTYPE_HOST_TO_DEVICE = 0x41;

        /**
         * Configuration Request Codes
         */
        private static final int SILABSER_IFC_ENABLE_REQUEST_CODE = 0x00;
        private static final int SILABSER_SET_BAUDDIV_REQUEST_CODE = 0x01;
        private static final int SILABSER_SET_LINE_CTL_REQUEST_CODE = 0x03;
        private static final int SILABSER_SET_MHS_REQUEST_CODE = 0x07;
        private static final int SILABSER_SET_BAUDRATE = 0x1E;
        private static final int SILABSER_FLUSH_REQUEST_CODE = 0x12;

        private static final int FLUSH_READ_CODE = 0x0a;
        private static final int FLUSH_WRITE_CODE = 0x05;

        /*
         * SILABSER_IFC_ENABLE_REQUEST_CODE
         */
        private static final int UART_ENABLE = 0x0001;
        private static final int UART_DISABLE = 0x0000;

        /*
         * SILABSER_SET_BAUDDIV_REQUEST_CODE
         */
        private static final int BAUD_RATE_GEN_FREQ = 0x384000;

        /*
         * SILABSER_SET_MHS_REQUEST_CODE
         */
        private static final int MCR_DTR = 0x0001;
        private static final int MCR_RTS = 0x0002;
        private static final int MCR_ALL = 0x0003;

        private static final int CONTROL_WRITE_DTR = 0x0100;
        private static final int CONTROL_WRITE_RTS = 0x0200;

        private UsbEndpoint mReadEndpoint;
        private UsbEndpoint mWriteEndpoint;

        public Cp21xxSerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
        }

        @Override
        public UsbSerialDriver getDriver() {
            return Cp21xxSerialDriver.this;
        }

        private int setConfigSingle(int request, int value) {
            //controlTransfer(int requestType, int request, int value,int index, byte[] buffer, int length, int timeout)
            //https://my.oschina.net/u/579890/blog/147074
            //http://han21912.lofter.com/post/c3919_29db48
            //http://blog.csdn.net/ct872587129/article/details/38776623
            return mConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, request, value, 0, null, 0, USB_WRITE_TIMEOUT_MILLIS);
        }

        @Override
        public void open(UsbDeviceConnection connection) throws IOException {
            if (mConnection != null) {
                throw new IOException("Already opened.");
            }
            mConnection = connection;
            boolean opened = false;
            boolean claim = false;
            try {
                for (int i = 0; i < mDevice.getInterfaceCount(); i++) {
                    UsbInterface usbIface = mDevice.getInterface(i);
                    if (mConnection.claimInterface(usbIface, true)) {
                        claim = true;
                    } else {
                        claim = false;
                    }
                }
                if (!claim)
                    throw new IOException("claim interface failed.");
                //                UsbInterface dataIface = mDevice.getInterface(mDevice.getInterfaceCount() - 1);
                for (int index = 0; index < mDevice.getInterfaceCount(); index++) {
                    UsbInterface dataIface = mDevice.getInterface(index);
                    for (int i = 0; i < dataIface.getEndpointCount(); i++) {
                        UsbEndpoint ep = dataIface.getEndpoint(i);
                        L.INSTANCE.d("断点类型：" + ep.getType());
                        //按硬件属性判断
                        //USB_ENDPOINT_XFER_BULK
                        //#define USB_ENDPOINT_XFER_CONTROL 0 -- 控制传输
                        //#define USB_ENDPOINT_XFER_ISOC 1 -- 等时传输
                        //#define USB_ENDPOINT_XFER_BULK 2 -- 块传输
                        //#define USB_ENDPOINT_XFER_INT 3 -- 中断传输
                        //                        if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                            mReadEndpoint = ep;
                        } else {
                            mWriteEndpoint = ep;
                        }
                    }
                }
                setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_ENABLE);
                setConfigSingle(SILABSER_SET_MHS_REQUEST_CODE, MCR_ALL | CONTROL_WRITE_DTR | CONTROL_WRITE_RTS);
                setConfigSingle(SILABSER_SET_BAUDDIV_REQUEST_CODE, BAUD_RATE_GEN_FREQ / DEFAULT_BAUD_RATE);
                opened = true;
            } finally {
                if (!opened) {
                    try {
                        close();
                    } catch (IOException e) {
                        // Ignore IOExceptions during close()
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (mConnection == null) {
                throw new IOException("Already closed");
            }
            try {
                setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_DISABLE);
                mConnection.close();
            } finally {
                mConnection = null;
            }
        }

        @Override
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            final int numBytesRead;
            synchronized (mReadBufferLock) {
                int readAmt = Math.min(dest.length, mReadBuffer.length);
                numBytesRead = mConnection.bulkTransfer(mReadEndpoint, mReadBuffer, readAmt, timeoutMillis);
                if (numBytesRead < 0) {
                    // This sucks: we get -1 on timeout, not 0 as preferred.
                    // We *should* use UsbRequest, except it has a bug/api oversight
                    // where there is no way to determine the number of bytes read
                    // in response :\ -- http://b.android.com/28023
                    return 0;
                }
                System.arraycopy(mReadBuffer, 0, dest, 0, numBytesRead);
            }
            return numBytesRead;
        }

        @Override
        public int write(byte[] src, int timeoutMillis) throws IOException {
            int offset = 0;
            while (offset < src.length) {
                final int writeLength;
                //length of data transferred for success
                final int amtWritten;
                synchronized (mWriteBufferLock) {
                    final byte[] writeBuffer;
                    writeLength = Math.min(src.length - offset, mWriteBuffer.length);
                    if (offset == 0) {
                        writeBuffer = src;
                    } else {
                        // bulkTransfer does not support offsets, make a copy.
                        System.arraycopy(src, offset, mWriteBuffer, 0, writeLength);
                        writeBuffer = mWriteBuffer;
                    }
                    if (mWriteEndpoint != null && mConnection != null) {
                        amtWritten = mConnection.bulkTransfer(mWriteEndpoint, writeBuffer, writeLength, timeoutMillis);
                    } else {
                        throw new IOException();
                    }
                }
                if (amtWritten <= 0) {
                    throw new IOException("Error writing =" + writeLength + ", bytes at offset =" + offset + " ,length=" + src.length);
                }
                L.INSTANCE.d("Wrote amt=" + amtWritten + " attempted=" + writeLength);
                offset += amtWritten;
            }
            return offset;
        }

        private void setBaudRate(int baudRate) throws IOException {
            byte[] data = new byte[]{(byte) (baudRate & 0xff), (byte) ((baudRate >> 8) & 0xff), (byte) ((baudRate >> 16) & 0xff), (byte) ((baudRate >> 24) & 0xff)};
            int ret = mConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, SILABSER_SET_BAUDRATE, 0, 0, data, 4, USB_WRITE_TIMEOUT_MILLIS);
            if (ret < 0) {
                throw new IOException("Error setting baud rate.");
            }
        }

        @Override
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            setBaudRate(baudRate);
            int configDataBits = 0;
            switch (dataBits) {
                case DATABITS_5:
                    configDataBits |= 0x0500;
                    break;
                case DATABITS_6:
                    configDataBits |= 0x0600;
                    break;
                case DATABITS_7:
                    configDataBits |= 0x0700;
                    break;
                case DATABITS_8:
                    configDataBits |= 0x0800;
                    break;
                default:
                    configDataBits |= 0x0800;
                    break;
            }
            switch (parity) {
                case PARITY_ODD:
                    configDataBits |= 0x0010;
                    break;
                case PARITY_EVEN:
                    configDataBits |= 0x0020;
                    break;
            }
            switch (stopBits) {
                case STOPBITS_1:
                    configDataBits |= 0;
                    break;
                case STOPBITS_2:
                    configDataBits |= 2;
                    break;
                default:
                    configDataBits |= 3;
                    break;
            }
            setConfigSingle(SILABSER_SET_LINE_CTL_REQUEST_CODE, configDataBits);
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
            return true;
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
            return true;
        }

        @Override
        public void setRTS(boolean value) throws IOException {
        }

        @Override
        public boolean purgeHwBuffers(boolean purgeReadBuffers, boolean purgeWriteBuffers) throws IOException {
            int value = (purgeReadBuffers ? FLUSH_READ_CODE : 0) | (purgeWriteBuffers ? FLUSH_WRITE_CODE : 0);
            if (value != 0) {
                setConfigSingle(SILABSER_FLUSH_REQUEST_CODE, value);
            }
            return true;
        }

    }

    @Keep
    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
        supportedDevices.put(Integer.valueOf(UsbId.VENDOR_SILABS), new int[]{UsbId.SILABS_CP2102, UsbId.SILABS_CP2105, UsbId.SILABS_CP2108, UsbId.SILABS_CP2110});
        return supportedDevices;
    }

}
