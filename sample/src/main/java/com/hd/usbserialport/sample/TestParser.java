package com.hd.usbserialport.sample;

import com.siheal.usbserialport.parser.Parser;

import org.jetbrains.annotations.NotNull;

/**
 * Created by hd on 2017/9/2 .
 *
 */
public class TestParser extends Parser {

    @Override
    public void asyncWrite() {
        super.asyncWrite();
        //处于while循环中
//        getWriteComplete().set(true);//跳出循环
    }

    @Override
    public void parser(@NotNull byte[] data) {
        //实现解析
//        complete();//完成测量
//        error();//测量错误
    }
}
