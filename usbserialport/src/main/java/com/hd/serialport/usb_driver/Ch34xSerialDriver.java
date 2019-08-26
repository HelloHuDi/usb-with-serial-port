/* Copyright 2014 Andreas Butti
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


/**
 * Driver for CH340, maybe also working with CH341, but not tested
 * See http://wch-ic.com/product/usb/ch340.asp
 *
 * @author Andreas Butti
 */
@Keep
public class Ch34xSerialDriver extends CommonUsbSerialDriver {

    private static final String TAG = Ch34xSerialDriver.class.getSimpleName();

    public Ch34xSerialDriver(UsbDevice mDevice) {
        super(mDevice);
    }

    @Override
    public UsbSerialPort setPort(UsbDevice mDevice) {
        return new Ch340SerialPort(mDevice, 0);
    }

    @NonNull
    @Override
    public String setDriverName() {
        return DriversType.USB_CH34xx;
    }

    public class Ch340SerialPort extends CommonUsbSerialPort {

        private static final int USB_TIMEOUT_MILLIS = 5000;
        private final int DEFAULT_BAUD_RATE = 9600;

        private static final int REQTYPE_HOST_TO_DEVICE = 0x40;
        private static final int CH341_REQ_WRITE_REG = 0x9A;

        // Baud rates values
        private static final int CH34X_300_1312 = 0xd980;
        private static final int CH34X_300_0f2c = 0xeb;

        private static final int CH34X_600_1312 = 0x6481;
        private static final int CH34X_600_0f2c = 0x76;

        private static final int CH34X_1200_1312 = 0xb281;
        private static final int CH34X_1200_0f2c = 0x3b;

        private static final int CH34X_2400_1312 = 0xd981;
        private static final int CH34X_2400_0f2c = 0x1e;

        private static final int CH34X_4800_1312 = 0x6482;
        private static final int CH34X_4800_0f2c = 0x0f;

        private static final int CH34X_9600_1312 = 0xb282;
        private static final int CH34X_9600_0f2c = 0x08;

        private static final int CH34X_19200_1312 = 0xd982;
        private static final int CH34X_19200_0f2c_rest = 0x07;

        private static final int CH34X_38400_1312 = 0x6483;

        private static final int CH34X_57600_1312 = 0x9883;

        private static final int CH34X_115200_1312 = 0xcc83;

        private static final int CH34X_230400_1312 = 0xe683;

        private static final int CH34X_460800_1312 = 0xf383;

        private static final int CH34X_921600_1312 = 0xf387;

        // Parity values
        private static final int CH34X_PARITY_NONE = 0xc3;
        private static final int CH34X_PARITY_ODD = 0xcb;
        private static final int CH34X_PARITY_EVEN = 0xdb;
        private static final int CH34X_PARITY_MARK = 0xeb;
        private static final int CH34X_PARITY_SPACE = 0xfb;

        private boolean dtr = false;
        private boolean rts = false;

        private UsbEndpoint mReadEndpoint;
        private UsbEndpoint mWriteEndpoint;

        public Ch340SerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
        }

        @Override
        public UsbSerialDriver getDriver() {
            return Ch34xSerialDriver.this;
        }

        @Override
        public void open(UsbDeviceConnection connection) throws IOException {
            if (mConnection != null) {
                throw new IOException("Already opened.");
            }

            mConnection = connection;
            boolean opened = false;
            try {
                for (int i = 0; i < mDevice.getInterfaceCount(); i++) {
                    UsbInterface usbIface = mDevice.getInterface(i);
                    if (mConnection.claimInterface(usbIface, true)) {
                        L.INSTANCE.d("claimInterface " + i + " SUCCESS");
                    } else {
                        L.INSTANCE.d("claimInterface " + i + " FAIL");
                    }
                }

                UsbInterface dataIface = mDevice.getInterface(mDevice.getInterfaceCount() - 1);
                for (int i = 0; i < dataIface.getEndpointCount(); i++) {
                    UsbEndpoint ep = dataIface.getEndpoint(i);
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                            mReadEndpoint = ep;
                        } else {
                            mWriteEndpoint = ep;
                        }
                    } else {
                        L.INSTANCE.d("ep.getType():" + ep.getType());
                    }
                }

                initialize();
                setBaudRate(DEFAULT_BAUD_RATE);

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
                mConnection.close();
            } finally {
                mConnection = null;
            }
        }


        @Override
        public int read(byte[] dest, int timeoutMillis) {
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
                int amtWritten;

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
                    if (mWriteEndpoint == null) {
                        if (mConnection != null) {
                            open(mConnection);
                        }
                    }
                    if (mWriteEndpoint != null && mConnection != null) {
                        amtWritten = mConnection.bulkTransfer(mWriteEndpoint, writeBuffer, writeLength, timeoutMillis);
                    } else {
                        amtWritten = 0;
                    }
                }
                if (amtWritten <= 0) {
                    throw new IOException("Error writing " + writeLength + " bytes at offset " + offset + " length=" + src.length);
                }
                offset += amtWritten;
            }
            return offset;
        }

        private int controlOut(int request, int value, int index) {
            return setControlCommandOut(request, value, index, null);
        }

        private int setControlCommandOut(int request, int value, int index, byte[] data) {
            int dataLength = 0;
            if (data != null) {
                dataLength = data.length;
            }
            return mConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, request, value, index, data, dataLength, USB_TIMEOUT_MILLIS);
        }

        private int controlIn(int request, int value, int index, byte[] buffer) {
            final int REQTYPE_HOST_TO_DEVICE = UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN;
            return mConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, request, value, index, buffer, buffer.length, USB_TIMEOUT_MILLIS);
        }

        private void checkState(String msg, int request, int value, int[] expected) throws IOException {
            byte[] buffer = new byte[expected.length];
            int ret = controlIn(request, value, 0, buffer);
            if (ret < 0) {
                throw new IOException("Faild send cmd [" + msg + "]");
            }
            if (ret != expected.length) {
                throw new IOException("Expected " + expected.length + " bytes, but get " + ret + " [" + msg + "]");
            }
        }

        private void writeHandshakeByte() throws IOException {
            if (controlOut(0xa4, ~((dtr ? 1 << 5 : 0) | (rts ? 1 << 6 : 0)), 0) < 0) {
                throw new IOException("Faild to set handshake byte");
            }
        }

        private void initialize() throws IOException {

            checkState("init #1", 0x5f, 0, new int[]{-1  /*0x27, 0x30*/, 0x00});

            if (controlOut(0xa1, 0, 0) < 0) {
                throw new IOException("init failed! #2");
            }

            setBaudRate(DEFAULT_BAUD_RATE);

            checkState("init #4", 0x95, 0x2518, new int[]{-1 /* 0x56, c3*/, 0x00});

            if (controlOut(0x9a, 0x2518, 0x0050) < 0) {
                throw new IOException("init failed! #5");
            }

            checkState("init #6", 0x95, 0x0706, new int[]{0xff, 0xee});

            if (controlOut(0xa1, 0x501f, 0xd90a) < 0) {
                throw new IOException("init failed! #7");
            }

            setBaudRate(DEFAULT_BAUD_RATE);

            writeHandshakeByte();

            checkState("init #10", 0x95, 0x0706, new int[]{-1/* 0x9f, 0xff*/, 0xee});
        }

       /* private void setBaudRate(int baudRate) throws IOException {
            int[] baud = new int[]{2400, 0xd901, 0x0038, 4800, 0x6402, 0x001f, 9600, 0xb202, 0x0013, 19200, 0xd902, 0x000d, 38400, 0x6403, 0x000a, 115200, 0xcc03, 0x0008};

            for (int i = 0; i < baud.length / 3; i++) {
                if (baud[i * 3] == baudRate) {
                    int ret = controlOut(0x9a, 0x1312, baud[i * 3 + 1]);
                    if (ret < 0) {
                        throw new IOException("Error setting baud rate. #1");
                    }
                    ret = controlOut(0x9a, 0x0f2c, baud[i * 3 + 2]);
                    if (ret < 0) {
                        throw new IOException("Error setting baud rate. #1");
                    }
                    return;
                }
            }
            throw new IOException("Baud rate " + baudRate + " currently not supported");
        }*/

        private void setBaudRate(int baudRate) throws IOException {
            int ret = 0;
            if (baudRate <= 300) {
                ret = setBaudRate(CH34X_300_1312, CH34X_300_0f2c); //300
            } else if (baudRate > 300 && baudRate <= 600) {
                ret = setBaudRate(CH34X_600_1312, CH34X_600_0f2c); //600
            } else if (baudRate > 600 && baudRate <= 1200) {
                ret = setBaudRate(CH34X_1200_1312, CH34X_1200_0f2c); //1200
            } else if (baudRate > 1200 && baudRate <= 2400) {
                ret = setBaudRate(CH34X_2400_1312, CH34X_2400_0f2c); //2400
            } else if (baudRate > 2400 && baudRate <= 4800) {
                ret = setBaudRate(CH34X_4800_1312, CH34X_4800_0f2c); //4800
            } else if (baudRate > 4800 && baudRate <= 9600) {
                ret = setBaudRate(CH34X_9600_1312, CH34X_9600_0f2c); //9600
            } else if (baudRate > 9600 && baudRate <= 19200) {
                ret = setBaudRate(CH34X_19200_1312, CH34X_19200_0f2c_rest); //19200
            } else if (baudRate > 19200 && baudRate <= 38400) {
                ret = setBaudRate(CH34X_38400_1312, CH34X_19200_0f2c_rest); //38400
            } else if (baudRate > 38400 && baudRate <= 57600) {
                ret = setBaudRate(CH34X_57600_1312, CH34X_19200_0f2c_rest); //57600
            } else if (baudRate > 57600 && baudRate <= 115200) {//115200
                ret = setBaudRate(CH34X_115200_1312, CH34X_19200_0f2c_rest);
            } else if (baudRate > 115200 && baudRate <= 230400) {//230400
                ret = setBaudRate(CH34X_230400_1312, CH34X_19200_0f2c_rest);
            } else if (baudRate > 230400 && baudRate <= 460800) { //460800
                ret = setBaudRate(CH34X_460800_1312, CH34X_19200_0f2c_rest);
            } else if (baudRate > 460800 && baudRate <= 921600) {
                ret = setBaudRate(CH34X_921600_1312, CH34X_19200_0f2c_rest);
            }
            if (ret == -1)
                L.INSTANCE.d("SetBaudRate failed!");
        }

        private int setBaudRate(int index1312, int index0f2c) throws IOException {
            if (setControlCommandOut(CH341_REQ_WRITE_REG, 0x1312, index1312, null) < 0)
                return -1;
            if (setControlCommandOut(CH341_REQ_WRITE_REG, 0x0f2c, index0f2c, null) < 0)
                return -1;
            checkState("set_baud_rate", 0x95, 0x0706, new int[]{0x9f, 0xee});
            if (setControlCommandOut(CH341_REQ_WRITE_REG, 0x2727, 0, null) < 0)
                return -1;
            return 0;
        }

        private void setParity(int parity) throws IOException {
            switch (parity) {
                case UsbSerialPort.PARITY_NONE:
                    setCh340xParity(CH34X_PARITY_NONE);
                    break;
                case UsbSerialPort.PARITY_ODD:
                    setCh340xParity(CH34X_PARITY_ODD);
                    break;
                case UsbSerialPort.PARITY_EVEN:
                    setCh340xParity(CH34X_PARITY_EVEN);
                    break;
                case UsbSerialPort.PARITY_MARK:
                    setCh340xParity(CH34X_PARITY_MARK);
                    break;
                case UsbSerialPort.PARITY_SPACE:
                    setCh340xParity(CH34X_PARITY_SPACE);
                    break;
                default:
                    break;
            }
        }

        private int setCh340xParity(int indexParity) throws IOException {
            if (setControlCommandOut(CH341_REQ_WRITE_REG, 0x2518, indexParity, null) < 0)
                return -1;

            checkState("set_parity", 0x95, 0x0706, new int[]{0x9f, 0xee});

            if (setControlCommandOut(CH341_REQ_WRITE_REG, 0x2727, 0, null) < 0)
                return -1;
            return 0;
        }

        @Override
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            setBaudRate(baudRate);
            setParity(parity);
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
            return dtr;
        }

        @Override
        public void setDTR(boolean value) throws IOException {
            dtr = value;
            writeHandshakeByte();
        }

        @Override
        public boolean getRI() throws IOException {
            return false;
        }

        @Override
        public boolean getRTS() throws IOException {
            return rts;
        }

        @Override
        public void setRTS(boolean value) throws IOException {
            rts = value;
            writeHandshakeByte();
        }

        @Override
        public boolean purgeHwBuffers(boolean purgeReadBuffers, boolean purgeWriteBuffers) throws IOException {
            return true;
        }

    }

    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
        supportedDevices.put(UsbId.VENDOR_QINHENG, new int[]{UsbId.QINHENG_HL340, UsbId.QINHENG_CH341});
        return supportedDevices;
    }
}