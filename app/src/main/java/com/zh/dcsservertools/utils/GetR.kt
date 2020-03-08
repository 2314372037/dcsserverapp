package com.zh.dcsservertools.utils

import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.request.GetRequest
import com.zh.dcsservertools.Constant
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
            okHttpClient.sslSocketFactory(
                sslParams1.sSLSocketFactory, sslParams1.trustManager)

            httpHeaders.put("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
//            httpHeaders.put("Accept-Encoding","gzip, deflate, br")
            httpHeaders.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            httpHeaders.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36 Edg/80.0.361.66")
            httpHeaders.put("Content-Type","application/x-www-form-urlencoded")
            httpHeaders.put("Accept","application/json, text/javascript, */*; q=0.01")

            OkGo.getInstance()
                .setCacheMode(CacheMode.NO_CACHE)
                .setRetryCount(0)
                .addCommonHeaders(httpHeaders)
        }

        fun <T>Get(url: String) : GetRequest<T>{
            InitCommon()
            val request:GetRequest<T> = OkGo.get(Constant.HOST_URL +url)
            return request
        }




    }
}