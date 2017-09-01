package com.siheal.usbserialport.parser

import com.hd.serialport.utils.L
import com.siheal.usbserialport.cache.UsbSerialPortCache
import com.siheal.usbserialport.result.PlaceholderResult
import java.util.*


/**
 * Created by hd on 2017/8/29 .
 *
 */
class TestSerialPortParser : Parser() {

    private var count = 100

    override fun asyncWrite() {
        super.asyncWrite()
        if (count > 0) {
            writeInitializationInstructAgain(10)
            count--
        }
        UsbSerialPortCache.newInstance(device!!.context).getSerialPortCache()
    }

    override fun parser(data: ByteArray) {
        L.d("接收数据：" + Arrays.toString(data) + "=" + count)
        if (count <= 0) {
            complete(PlaceholderResult("成功"), true)
        }
    }
}