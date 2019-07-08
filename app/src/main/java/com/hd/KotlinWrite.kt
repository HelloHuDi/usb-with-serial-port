package com.hd

import android.support.v7.app.AppCompatActivity
import com.hd.serialport.listener.SerialPortMeasureListener
import com.hd.serialport.method.DeviceMeasureController
import com.hd.serialport.param.SerialPortMeasureParameter
import java.io.OutputStream


/**
 * Created by hd on 2019-07-08 .
 *
 */

class KotlinWrite : AppCompatActivity() {
    
    fun testA(){
        val customTag= "自定义tag"
        DeviceMeasureController.measure(SerialPortMeasureParameter(tag = customTag),object : SerialPortMeasureListener{
            
            override fun measuring(tag: Any?, path: String, data: ByteArray) {
                //根据tag响应
            }
    
            override fun write(tag: Any?, outputStream: OutputStream) {
                //根据tag响应
            }
    
            override fun measureError(tag: Any?, message: String) {
                //根据tag响应
            }
        })
        
        //停止指定的串口，不传或者传null停止所有已经打开的串口
        DeviceMeasureController.write(listOf(),customTag)
    }
}
