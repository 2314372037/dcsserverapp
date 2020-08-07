package com.zh.dcsservertools

import android.app.Application
import com.tencent.mmkv.MMKV

class InitApp : Application() {
    companion object{
        lateinit var app:InitApp
    }

    override fun onCreate() {
        super.onCreate()
        app=this
        MMKV.initialize(this)
    }
}