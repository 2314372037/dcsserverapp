package com.zh.dcsservertools.utils

import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.lang.Exception

class MMKVHelper {

    companion object{
        private val LOCAL_ALLLIST_DATA_KEY:String = "00001"
        private val LOCAL_USER_INFO_KEY:String = "00003"

        val JSON_KEY_USERNAME:String = "00004"
        val JSON_KEY_PASSWORD:String = "00005"

        fun SaveAll(string: String){
            MMKV.defaultMMKV().putString(LOCAL_ALLLIST_DATA_KEY,string)
        }

        fun GetAll() :String?{
            return MMKV.defaultMMKV().getString(LOCAL_ALLLIST_DATA_KEY,null)
        }

        fun ClearAll(){
            MMKV.defaultMMKV().removeValueForKey(LOCAL_ALLLIST_DATA_KEY)
        }

        /***
         * 保存用户信息
         * @param string 格式为jsonobject{u:"username",p:"password"}
         */
        fun SaveUserInfo(username: String,password:String){
            try {
                val jsonObject= JSONObject()
                jsonObject.put(JSON_KEY_USERNAME,username)
                jsonObject.put(JSON_KEY_PASSWORD,password)
                MMKV.defaultMMKV().putString(LOCAL_USER_INFO_KEY,jsonObject.toString())
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        fun GetUserInfo() : JSONObject?{
            return try{
                val json = MMKV.defaultMMKV().getString(LOCAL_USER_INFO_KEY,null)
                if (json==null){
                    return null
                }else{
                    return JSONObject(json)
                }
            }catch (e:Exception){
                e.printStackTrace()
                null
            }
        }

        fun ClearUserInfo(){
            MMKV.defaultMMKV().removeValueForKey(LOCAL_USER_INFO_KEY)
        }

    }

}