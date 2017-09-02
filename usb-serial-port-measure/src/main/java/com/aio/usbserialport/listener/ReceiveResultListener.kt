package com.aio.usbserialport.listener

import android.support.annotation.NonNull
import com.aio.usbserialport.result.ParserResult

/**
 * Created by hd on 2017/8/31.

 */
interface ReceiveResultListener {

    /**
     * receive aio device response data
     */
    fun receive(@NonNull parserResult: ParserResult)

    /**
     * measure error

     */
    fun error(@NonNull msg: String)
}
