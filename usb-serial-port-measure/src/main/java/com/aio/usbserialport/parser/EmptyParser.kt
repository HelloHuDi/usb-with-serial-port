package com.aio.usbserialport.parser

import com.hd.serialport.utils.L
import java.util.*


/**
 * Created by hd on 2017/9/4 .
 *
 */
open class EmptyParser:Parser(){

    override fun parser(data: ByteArray) {
        L.d("empty parser :"+ Arrays.toString(data))
    }

}