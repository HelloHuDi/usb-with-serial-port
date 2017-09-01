package com.siheal.usbserialport.listener

import android.support.annotation.NonNull
import com.siheal.usbserialport.result.ParserResult

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
