package com.zh.dcsservertools

import android.app.Application
import com.lzy.okgo.OkGo
import com.tencent.mmkv.MMKV

class InitApp : Application() {

    companion object{
        lateinit var app:InitApp

        fun getInstance() : Application{
            return app
        }

    }


    override fun onCreate() {
        super.onCreate()
        app=this
        Init()
    }

    private fun Init(){
        MMKV.initialize(this)
        OkGo.getInstance().init(this)
    }




}