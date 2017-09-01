package com.siheal.usbserialport.utils

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by hd on 2017/8/31 .
 *
 */
object ParcelableUtil{

    fun write(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parcel.setDataPosition(0)
        parcelable.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun read(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return parcel
    }
}