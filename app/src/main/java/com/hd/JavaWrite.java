package com.hd;

import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.hd.serialport.listener.SerialPortMeasureListener;
import com.hd.serialport.method.DeviceMeasureController;
import com.hd.serialport.param.SerialPortMeasureParameter;
import com.hd.serialport.usb_driver.UsbSerialDriver;
import com.hd.serialport.usb_driver.extend.UsbExtendDriver;
import com.hd.usbserialport.sample.TestUsbDriver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hd on 2019-07-08 .
 */
public class JavaWrite extends AppCompatActivity {

    public void testA(){
        DeviceMeasureController.INSTANCE.measure(new SerialPortMeasureParameter(), new SerialPortMeasureListener(){
            @Override
            public void measureError(@Nullable Object tag, @NotNull String message) {

            }

            @Override
            public void write(@Nullable Object tag, @NotNull OutputStream outputStream) {

            }

            @Override
            public void measuring(@Nullable Object tag, @NotNull String path, @NotNull byte[] data) {

            }
        });

        DeviceMeasureController.INSTANCE.write(new ArrayList<byte[]>(), null);
    }

    public void testB(){
        List<Pair<String, Class<? extends UsbSerialDriver>>> list = Arrays.asList(
                new Pair<String, Class<? extends UsbSerialDriver>>("custom", TestUsbDriver.class));
        new UsbExtendDriver.Extender().setDrivers(list).extend();
    }
}
