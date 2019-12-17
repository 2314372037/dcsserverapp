package com.zh.test1

import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.request.GetRequest
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


class GetR {
    companion object{
        val okHttpClient:OkHttpClient.Builder = OkHttpClient.Builder()
        val sslParams1 = HttpsUtils.getSslSocketFactory()

        val httpHeaders:HttpHeaders = HttpHeaders()

        fun InitCommon(){
            okHttpClient.connectTimeout(10,TimeUnit.SECONDS)
            okHttpClient.writeTimeout(10,TimeUnit.SECONDS)
            okHttpClient.readTimeout(10,TimeUnit.SECONDS)
            okHttpClient.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)

            httpHeaders.put("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
            httpHeaders.put("Accept","application/json, text/javascript, */*; q=0.01")
            httpHeaders.put("TE","Trailers")
            httpHeaders.put("X-Requested-With","XMLHttpRequest")

            OkGo.getInstance()
                .setCacheMode(CacheMode.NO_CACHE)
                .setRetryCount(0)
                .addCommonHeaders(httpHeaders)
        }

        fun <T>Get(url: String) : GetRequest<T>{
            InitCommon()
            val request:GetRequest<T> = OkGo.get(Constant.HOST_URL+url)
            return request
        }




    }
}