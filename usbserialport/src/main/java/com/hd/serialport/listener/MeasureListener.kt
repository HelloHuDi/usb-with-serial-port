package com.hd.serialport.listener


/**
 * Created by hd on 2017/8/22 .
 *
 */
interface MeasureListener {
    
    /**
     * hint measure error message
     */
    fun measureError(tag: Any?, message: String)
    
}
