package com.zh.dcsservertools.bean

data class baiduTranslateBean(
    var from: String?,
    var to: String?,
    var trans_result: List<TransResult?>?
) {
    data class TransResult(
        var dst: String?,
        var src: String?
    )
}