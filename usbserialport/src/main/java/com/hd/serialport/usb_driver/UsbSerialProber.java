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

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Pair;

import com.hd.serialport.config.DriversType;
import com.hd.serialport.usb_driver.extend.UsbExtendDriver;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author mike wakerly (opensource@hoho.com)
 */
public class UsbSerialProber {

    private final ProbeTable mProbeTable;

    public UsbSerialProber(ProbeTable probeTable) {
        mProbeTable = probeTable;
    }

    public static UsbSerialProber getDefaultProber() {
        return new UsbSerialProber(getDefaultProbeTable());
    }

    private static ProbeTable getDefaultProbeTable() {
        final ProbeTable probeTable = new ProbeTable();
        final List<Pair<String, Class<? extends UsbSerialDriver>>> drivers = new UsbExtendDriver().getExtendDrivers();

        List<String> defaultTypeList = Arrays.asList(DriversType.USB_CDC_ACM, DriversType.USB_CP21xx,//
                                                     DriversType.USB_FTD, DriversType.USB_PL2303,//
                                                     DriversType.USB_CH34xx);

        List<Class<? extends CommonUsbSerialDriver>> defaultDriverList = Arrays.asList(CdcAcmSerialDriver.class, Cp21xxSerialDriver.class,//
                                                                                       FtdiSerialDriver.class, ProlificSerialDriver.class,//
                                                                                       Ch34xSerialDriver.class);
        if(null != drivers && !drivers.isEmpty()) {
            int index;
            for (Pair<String, Class<? extends UsbSerialDriver>> pair : drivers) {
                index = defaultTypeList.indexOf(pair.first);
                if (index > 0) {
                    defaultDriverList.remove(index);
                }
                probeTable.addDriver(pair.second);
            }
        }
        for (Class<? extends CommonUsbSerialDriver> driver : defaultDriverList) {
            probeTable.addDriver(driver);
        }
        return probeTable;
    }

    /**
     * Finds and builds all possible {@link UsbSerialDriver UsbSerialDrivers}
     * from the currently-attached {@link UsbDevice} hierarchy. This method does
     * not require permission from the Android USB system, since it does not
     * open any of the devices.
     *
     * @param usbManager
     *
     * @return a list, possibly empty, of all compatible drivers
     */
    @Keep
    public List<UsbSerialDriver> findAllDrivers(final UsbManager usbManager) {
        final List<UsbSerialDriver> result = new ArrayList<UsbSerialDriver>();
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice usbDevice = deviceIterator.next();
            UsbSerialDriver driver = probeDevice(usbDevice);
            if (driver != null) {
                result.add(driver);
            }
        }
        return result;
    }

    /**
     * Probes a single device for a compatible driver.
     *
     * @param usbDevice the usb device to probe
     *
     * @return a new {@link UsbSerialDriver} compatible with this device, or
     * {@code null} if none available.
     */
    @Keep
    public UsbSerialDriver probeDevice(final UsbDevice usbDevice) {
        final int vendorId = usbDevice.getVendorId();
        final int productId = usbDevice.getProductId();
        final Class<? extends UsbSerialDriver> driverClass = mProbeTable.findDriver(vendorId, productId);
        return getUsbSerialDriver(usbDevice, driverClass);
    }

    @Keep
    public UsbSerialDriver probeDevice(final UsbDevice usbDevice, String driverName) {
        if(TextUtils.isEmpty(driverName))return probeDevice(usbDevice);
        UsbSerialDriver driver = null;
        Map<Pair<Integer, Integer>, Class<? extends UsbSerialDriver>> table = mProbeTable.getProbeTable();
        for (Map.Entry<Pair<Integer, Integer>, Class<? extends UsbSerialDriver>> entry : table.entrySet()) {
            driver = getUsbSerialDriver(usbDevice, entry.getValue());
            if (null != driver && driverName.equals(driver.getDeviceType().getValue())) {
                return driver;
            }
        }
        return driver;
    }

    private UsbSerialDriver getUsbSerialDriver(UsbDevice usbDevice, Class<? extends UsbSerialDriver> driverClass) {
        if (driverClass != null) {
            final UsbSerialDriver driver;
            try {
                final Constructor<? extends UsbSerialDriver> constructor = driverClass.getConstructor(UsbDevice.class);
                driver = constructor.newInstance(usbDevice);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return driver;
        }
        return null;
    }

    public UsbSerialDriver convertDriver(UsbDevice usbDevice, String driverName) {
        UsbSerialDriver driver = probeDevice(usbDevice);
        if (driver == null) {
            driver = probeDevice(usbDevice, driverName);
        }
        if (driver == null)
            throw new NullPointerException("unknown usb device type name : " + driverName);
        return driver;
    }
}
