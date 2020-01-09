package com.hd.serialport.usb_driver.extend;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.hd.serialport.usb_driver.UsbSerialDriver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by hd on 2019-08-26 .
 * <p>
 * usb驱动扩展
 * <p>
 * 1.扩展的serial driver 需要继承 {@link com.hd.serialport.usb_driver.CommonUsbSerialDriver}
 * 2.扩展的serial port 需要继承 {@link com.hd.serialport.usb_driver.CommonUsbSerialPort}
 * 3.扩展的serial driver类方法 getDeviceType，理论上需要写成 ：
 * ```
 * @Override public String setDriverName() {
 * return "custom value";
 * }
 * ```
 * 4.扩展的serial port 类需要添加`getSupportedDevices`方法 ,如下：
 * ```
 * public static Map<Integer, int[]> getSupportedDevices() {
 * final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
 * supportedDevices.put(UsbId.VENDOR_QINHENG, new int[]{UsbId.QINHENG_HL340, UsbId.QINHENG_CH341});
 * return supportedDevices;
 * }
 * ```
 */
@SuppressWarnings("ALL")
public class UsbExtendDriver {

    private static List<Pair<String, Class<? extends UsbSerialDriver>>> extendDrivers;

    public UsbExtendDriver() { }

    private UsbExtendDriver(Extender extender) {
        extendDrivers = extender.drivers;
    }

    private void clearExtendDrivers(){
        extendDrivers.clear();
        extendDrivers = null;
    }

    public List<Pair<String, Class<? extends UsbSerialDriver>>> getExtendDrivers(){
        return extendDrivers;
    }

    public static class Extender {

        public Extender Extender() { return new Extender();}

        private List<Pair<String, Class<? extends UsbSerialDriver>>> drivers;

        /**
         * e.g.
         * List list = new ArrayList();
         * list.add(new Pair("custom1 value",CustomDriver1.class));
         * list.add(new Pair("custom2 value",CustomDriver2.class));
         * list.add(new Pair("custom3 value",CustomDriver3.class));
         * setDrivers(list);
         */
        public Extender setDrivers(@NonNull List<Pair<String, Class<? extends UsbSerialDriver>>> drivers) {
            this.drivers = drivers;
            return this;
        }

        public void clearExtendDrivers(){
            new UsbExtendDriver().clearExtendDrivers();
        }

        public void extend() {
            drivers.forEach(new Consumer<Pair<String, Class<? extends UsbSerialDriver>>>() {
                @Override
                public void accept(Pair<String, Class<? extends UsbSerialDriver>> driver) {
                    if (driver.first.isEmpty()) throw new NullPointerException("custom extended usb driver name must be not null !!");
                    try {
                        Method method = driver.second.getMethod("getSupportedDevices");
                    } catch (Exception e) {
                        throw new RuntimeException("custom extended usb driver must add a method named 'getSupportedDevices' !");
                    }
                }
            });
            new UsbExtendDriver(this);
        }
    }

}
