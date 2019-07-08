package com.hd;

import android.support.v7.app.AppCompatActivity;

import com.hd.serialport.listener.SerialPortMeasureListener;
import com.hd.serialport.method.DeviceMeasureController;
import com.hd.serialport.param.SerialPortMeasureParameter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.ArrayList;

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
}
