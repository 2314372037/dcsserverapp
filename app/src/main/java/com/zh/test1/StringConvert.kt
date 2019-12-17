package com.zh.test1

import com.lzy.okgo.callback.StringCallback
import okhttp3.Response

abstract class StringConvert : StringCallback() {

    override fun convertResponse(response: Response): String? {
        var str = response.body()!!.string()
        return str
    }

    override fun onSuccess(response: com.lzy.okgo.model.Response<String?>) {

    }

}