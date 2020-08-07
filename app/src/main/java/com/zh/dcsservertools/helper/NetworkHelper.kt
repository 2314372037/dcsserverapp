package com.zh.dcsservertools.helper

import android.app.Activity
import android.util.Log
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/***
 * created by zhanghao
 * email zh2314372037@outlook.com
 * @time 2020/8/7 10:07
 */
class NetworkHelper(private val activity: Activity) {
    companion object {
        val HOST = "https://www.digitalcombatsimulator.com"//主机地址
        val SERVER_LIST_URL = "/cn/personal/server/?ajax=y&_="//=后面是时间戳，单位毫秒
        val SINA = "https://weibo.com/hzhang2314"//我的微博账号
        val GITHUB = "https://github.com/2314372037/dcsserverapp"//项目git地址

        val baiduFanyiAppkey = ""//百度翻译相关
        val baiduFanyiAppid = ""//百度翻译相关
    }

    fun response(
        url: String,
        header: HashMap<String, Any>? = null,
        params: HashMap<String, Any>? = null,
        success: ((data: String, Map<String, List<String>>) -> Unit)? = null,
        error: ((code: Int, msg: String) -> Unit)? = null,
        requestMethod: String = "GET"
    ) {
        thread {
            try {
                val mUrl = URL(HOST + url)
                val httpURLConnection: HttpURLConnection =
                    mUrl.openConnection() as HttpURLConnection
                httpURLConnection.connectTimeout = 30000//30秒
                httpURLConnection.readTimeout = 30000//30秒
                httpURLConnection.requestMethod = requestMethod

                if (true) {
                    //遍历请求头
                    val keys = header?.iterator()
                    if (keys != null) {
                        for (i in keys) {
                            val k = i.key
                            val v: String = i.value.toString()
                            httpURLConnection.setRequestProperty(k, v)
                        }
                    }
                }

                var outputStream:OutputStream? = null
                if (requestMethod == "POST") {
                    httpURLConnection.doOutput = true
                    //遍历参数
                    val keys = params?.iterator()
                    if (keys != null) {
                        val sb = StringBuffer()
                        for (i in keys) {
                            val k = i.key
                            val v: String = i.value.toString()
                            sb.append(k).append("=").append(v).append("&")
                        }

                        val bypes = sb.toString().toByteArray()
                        outputStream = httpURLConnection.getOutputStream()
                        outputStream?.write(bypes)
                    }
                }

                val inputStream = httpURLConnection.getInputStream()
                val data = String(inputStream.readBytes())//请求到的响应体
                val headerFields = httpURLConnection.headerFields

                inputStream.close()
                outputStream?.close()
                activity.runOnUiThread {
                    success?.invoke(data, headerFields)
                    Log.d("调试", "响应data:$data")
                }
            } catch (e: Exception) {//如果要得到详细请求错误code，还要细分Exception
                e.printStackTrace()
                activity.runOnUiThread {
                    error?.invoke(0, e.message.toString())
                }
            }
        }
    }

//    httpHeaders.put("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
////            httpHeaders.put("Accept-Encoding","gzip, deflate, br")
//    httpHeaders.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//    httpHeaders.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36 Edg/80.0.361.66")
//    httpHeaders.put("Content-Type","application/x-www-form-urlencoded")
//    httpHeaders.put("Accept","application/json, text/javascript, */*; q=0.01")

}