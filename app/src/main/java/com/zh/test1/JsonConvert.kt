package com.zh.test1

import android.util.Log
import com.google.gson.Gson
import com.lzy.okgo.callback.AbsCallback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.ParameterizedType

abstract class JsonConvert<T> : AbsCallback<T>() {
    override fun convertResponse(response: Response?): T {
        val responseBody = response?.body()

        val json: String? = responseBody?.string()

        if (BuildConfig.DEBUG) {
            Log.d(JsonConvert::class.java.simpleName, json!!)
        }

        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

        if (!isjson(json!!)){
            JSONException("解析错误")
        }

        return Gson().fromJson(json, type)
    }

    override fun onSuccess(response: com.lzy.okgo.model.Response<T>?) {

    }


    fun isjson(string: String): Boolean {
        return try {
            JSONObject(string)
            true
        } catch (e: JSONException) {
            false
        }
    }


}