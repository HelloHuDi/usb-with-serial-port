package com.aio.usbserialport.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.hd.serialport.utils.L
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


/**
 * Created by hd on 2017/8/31 .
 * the only ID identification equipment
 */
class DeviceIdUtil private constructor(private val context: Context) {

    companion object {

        fun newInstance(context: Context): DeviceIdUtil {
            return DeviceIdUtil(context)
        }
    }

    private val DEFAULT_NAME = "hd"

    /**
     * get pseudo unique ID
     */
    val uniqueID: String
        get() {
            val stringBuilder = formatEmptyString(deviceID) + //
                    formatEmptyString(androidID) + //
                    formatEmptyString(serialNumber) + //
                    formatEmptyString(installDeviceID) + //
                    formatEmptyString(customID)
            return Md5Encrypt(stringBuilder)
        }

    private fun formatEmptyString(str: String?): String {
        L.d("get device's id :$str")
        return if (str == null || str.isEmpty()) DEFAULT_NAME else str
    }

    private fun Md5Encrypt(plainText: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(plainText.toByteArray())
            val b = md.digest()
            var i: Int
            val sb = StringBuilder("")
            for (offset in b.indices) {
                i = b[offset].toInt()
                if (i < 0) {
                    i += 256
                }
                if (i < 16) {
                    sb.append("0")
                }
                sb.append(Integer.toHexString(i))
            }
            //32位加密
            return sb.toString()//buf.toString().substring(8, 24); //16位的加密
        } catch (e: NoSuchAlgorithmException) {
            return plainText
        }

    }

    /**
     * Android系统为开发者提供的用于标识手机设备的串号,非手机设备无法获取
     */
    val deviceID: String
        @SuppressLint("HardwareIds", "MissingPermission")
        get() = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId

    /**
     * 在设备首次启动时，系统会随机生成一个64位的数字，
     * 并把这个数字以16进制字符串的形式保存下来，这个16进制的字符串就是ANDROID_ID,当设备被wipe后该值会被重置。
     * 注意：不同的设备可能会产生相同的ANDROID_ID，有些设备返回的值为null,厂商问题.
     * 对于CDMA设备，ANDROID_ID和TelephonyManager.getDeviceId() 返回相同的值。
     */
    val androidID: String
        get() = Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    /**
     * Android系统2.3版本以上可以通过下面的方法得到Serial Number，且非手机设备也可以通过该接口获取
     */
    val serialNumber: String
        @SuppressLint("HardwareIds")
        get() = android.os.Build.SERIAL

    /**
     * 在程序安装后第一次运行时生成一个ID，该方式和设备唯一标识不一样，
     * 不同的应用程序会产生不同的ID，同一个程序重新安装也会不同。
     * 所以这不是设备的唯一ID，但是可以保证每个用户的ID是不同的。
     * 可以说是用来标识每一份应用程序的唯一ID（即Install device ID），可以用来跟踪应用的安装数量等
     */
    val installDeviceID: String
        get() {
            var sID = ""
            val installation = File(context.filesDir, "INSTALLATION")
            try {
                if (!installation.exists()) {
                    val out = FileOutputStream(installation)
                    val id = UUID.randomUUID().toString()
                    out.write(id.toByteArray())
                    out.close()
                }
                sID = readInstallationFile(installation)
            } catch (ignored: Exception) {
            }
            return sID
        }

    /**
     * 通过读取设备的ROM版本号、厂商名、CPU型号和其他硬件信息来组合出一串号码作为标识
     */
    //2+13 位
    //API>=9 使用serial号
    val customID: String
        get() {
            var serial = "hd_serial"
            val m_szDevIDShort = DEFAULT_NAME + Build.BOARD.length % 10 + Build.BRAND.length % 10 +

                    Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 +

                    Build.DISPLAY.length % 10 + Build.HOST.length % 10 +

                    Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +

                    Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +

                    Build.TAGS.length % 10 + Build.TYPE.length % 10 +

                    Build.USER.length % 10
            try {
                serial = android.os.Build::class.java.getField("SERIAL").get(null).toString()
            } catch (ignored: Exception) {
            }

            return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        }

    @Throws(IOException::class)
    private fun readInstallationFile(installation: File): String {
        val f = RandomAccessFile(installation, "r")
        val bytes = ByteArray(f.length().toInt())
        f.readFully(bytes)
        f.close()
        return String(bytes)
    }
}
