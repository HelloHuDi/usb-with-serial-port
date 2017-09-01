/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hd.serialport.utils;

/**
 * Clone of Android's HexDump class, for use in debugging. Cosmetic changes only.
 */
public class HexDump {
    private final static char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String hextoString(byte[] array) {
        return hextoString(array, 0, array.length);
    }

    public static String hextoString(byte[] array, int offset, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            result.append(HEX_DIGITS[array[i] & 0x0F]);
        }
        return result.toString();
    }

    /**
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @param bytearray byte[]
     *
     * @return String
     */
    public static String bytetoString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (byte aBytearray : bytearray) {
            temp = (char) aBytearray;
            result += temp;
        }
        return result;
    }

    public static String toHexString(byte b) {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(byte[] array) {
        return toHexString(array, 0, array.length);
    }

    public static String toHexString(byte[] array, int offset, int length) {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }

    public static String toHexString(short i) {
        return toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(byte b) {
        byte[] array = new byte[1];
        array[0] = b;
        return array;
    }

    public static byte[] toByteArray(int i) {
        byte[] array = new byte[4];
        array[3] = (byte) (i & 0xFF);
        array[2] = (byte) ((i >> 8) & 0xFF);
        array[1] = (byte) ((i >> 16) & 0xFF);
        array[0] = (byte) ((i >> 24) & 0xFF);

        return array;
    }

    public static byte[] toByteArray(short i) {
        byte[] array = new byte[2];
        array[1] = (byte) (i & 0xFF);
        array[0] = (byte) ((i >> 8) & 0xFF);

        return array;
    }

    private static int toByte(char c) {
        if (c >= '0' && c <= '9')
            return (c - '0');
        if (c >= 'A' && c <= 'F')
            return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f')
            return (c - 'a' + 10);

        throw new RuntimeException("Invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] buffer = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString.charAt(i + 1)));
        }

        return buffer;
    }

    /**
     * 将传入的十六进制字符串按位两两异或 最终得到异或校验和
     *
     * @param hexStr
     * @return
     */
    public static String checkSum(String hexStr) {
        if (hexStr.length() < 2) {
            // 保证必须有one byte十六进制字符
            return null;
        }
        String x = null;
        for (int i = 0; i < hexStr.length() / 2 - 1; i++) {
            if (i == 0) {
                x = hexStr.substring(i, i + 2);
            }
            String y = hexStr.substring(i * 2 + 2, i * 2 + 4);
            x = xor(x, y);
        }
        return x;
    }

    /**
     * 将传入的两个one byte十六进制字符异或
     *
     * @param strHex_X
     * @param strHex_Y
     * @return
     */
    private static String xor(String strHex_X, String strHex_Y) {
        // 将x、y转成二进制形式
        String anotherBinary = Integer.toBinaryString(Integer.valueOf(strHex_X,
                                                                      16));
        String thisBinary = Integer.toBinaryString(Integer
                                                           .valueOf(strHex_Y, 16));
        String result = "";
        // 判断是否为8位二进制，否则左补零
        if (anotherBinary.length() != 8) {
            for (int i = anotherBinary.length(); i < 8; i++) {
                anotherBinary = "0" + anotherBinary;
            }
        }
        if (thisBinary.length() != 8) {
            for (int i = thisBinary.length(); i < 8; i++) {
                thisBinary = "0" + thisBinary;
            }
        }
        // 异或运算
        for (int i = 0; i < anotherBinary.length(); i++) {
            // 如果相同位置数相同，则补0，否则补1
            if (thisBinary.charAt(i) == anotherBinary.charAt(i))
                result += "0";
            else {
                result += "1";
            }
        }
        return Integer.toHexString(Integer.parseInt(result, 2));
    }

    /**
     * 字符串的16进制累加和
     */
    public static String makeChecksum(String data) {
        if (data == null || data.equals("")) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            System.out.println(s);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        //用256求余最大是255，即16进制的FF
        int mod = total % 256;
        String hex = Integer.toHexString(mod);
        len = hex.length();
        // 如果不够校验位的长度，补0,这里用的是两位校验
        if (len < 2) {
            hex = "0" + hex;
        }
        return hex;
    }
}
