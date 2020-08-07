package com.zh.dcsservertools.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

class MyWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    private lateinit var onLoadingFinshListener:(title: String)->Unit

    init {
        initWebView()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return true
            }


            override fun onPageFinished(view: WebView, url: String) {
                if (this@MyWebView::onLoadingFinshListener.isInitialized){
                    onLoadingFinshListener.invoke(view.title!!)
                }
            }
        }

        settings.javaScriptEnabled = true
        //设置js可以直接打开窗口，如window.open()，默认为false
        settings.javaScriptCanOpenWindowsAutomatically = true
        // 使用localStorage则必须打开
        settings.domStorageEnabled = true
        settings.setGeolocationEnabled(true)
    }

    fun loadData(data: String) {
        loadDataWithBaseURL(null, data, "text/html", "utf-8", null)
    }


    fun setOnLoadingFinshListener(callback: (String) -> Unit){
        onLoadingFinshListener=callback
    }


}