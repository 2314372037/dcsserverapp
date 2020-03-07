package com.zh.dcsservertools

import java.util.regex.Matcher
import java.util.regex.Pattern

class utils {

    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    fun isContainChinese(str: String?): Boolean {
        val p: Pattern = Pattern.compile("[\u4e00-\u9fa5]")
        val m: Matcher = p.matcher(str)
        return m.find()
    }

}