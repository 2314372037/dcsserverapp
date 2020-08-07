package com.zh.dcsservertools.helper

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class Utils {

    companion object{
        /**
         * 判断字符串中是否包含中文
         * @param str
         * 待校验字符串
         * @return 是否为中文
         * @warn 不能校验是否为中文标点符号
         */
        fun isContainChinese(str: String?): Boolean {
            if (str.isNullOrEmpty()){
                return false
            }
            val p: Pattern = Pattern.compile("[\u4e00-\u9fa5]")
            val m: Matcher = p.matcher(str)
            return m.find()
        }

        fun stringToMD5(plainText: String): String? {
            var secretBytes: ByteArray? = null
            secretBytes = try {
                MessageDigest.getInstance("md5").digest(
                    plainText.toByteArray()
                )
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("没有这个md5算法！")
            }
            var md5code: String = BigInteger(1, secretBytes).toString(16)
            for (i in 0 until 32 - md5code.length) {
                md5code = "0$md5code"
            }
            return md5code
        }

        fun getNumber(len:Int):String{
            var num = ""
            val nnn = "0123456789"
            for (i in 0..len){
                val r = Random().nextInt(10)
                num+=nnn[r]
            }
            return num
        }
    }

}