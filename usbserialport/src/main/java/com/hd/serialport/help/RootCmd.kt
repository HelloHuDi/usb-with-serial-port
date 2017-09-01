package com.hd.serialport.help


/**
 * Created by hd on 2017/8/22.
 * 执行Linux命令,修改usb权限为可读
 */
object RootCmd {

    // 开放/dev目录下所有文件的读、写、执行权限， " -R " 指令勿随意指定，较危险操作;
    // 其他指令例如下 ：
    // <p>
    // "chmod 777 /dev;"
    // + "chmod 777 /dev/;"
    // + "chmod 777 /dev/usb/;"
    // + "chmod 777 /dev/bus/;"
    // + "chmod 777 /dev/bus/usb/;"
    // + "chmod 777 /dev/bus/usb/0*;"
    // + "chmod 777 /dev/bus/usb/001/*;"
    // + "chmod 777 /dev/bus/usb/002/*;"
    // + "chmod 777 /dev/bus/usb/003/*;"
    // + "chmod 777 /dev/bus/usb/004/*;"
    // + "chmod 777 /dev/bus/usb/005/*;"
    private val param_all = "chmod 777 /dev;" + "chmod -R 777 /dev/*"

    @JvmOverloads fun execRootCmdOrder(paramString: String = param_all): Int {
        try {
            val su = Runtime.getRuntime().exec("/system/bin/su")
            su.outputStream.write(paramString.toByteArray())
            su.outputStream.flush()
            su.outputStream.write("\nexit\n".toByteArray())
            su.outputStream.flush()
            su.waitFor()
            return su.exitValue()
        } catch (localException: Exception) {
            localException.printStackTrace()
        }
        return 0
    }
}
