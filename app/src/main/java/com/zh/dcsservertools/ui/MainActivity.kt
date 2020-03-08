package com.zh.dcsservertools.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.GetRequest
import com.lzy.okgo.request.base.Request
import com.zh.dcsservertools.BuildConfig
import com.zh.dcsservertools.Constant
import com.zh.dcsservertools.R
import com.zh.dcsservertools.adapter.ServiceListForAllAdapter
import com.zh.dcsservertools.adapter.ServiceListForMyAdapter
import com.zh.dcsservertools.bean.ServiceListBean
import com.zh.dcsservertools.bean.baiduTranslateBean
import com.zh.dcsservertools.utils.*
import jp.wasabeef.richeditor.RichEditor
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_loading.*
import okhttp3.Headers
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Field

class MainActivity : AppCompatActivity() {

    lateinit var serviceListForAllAdapter: ServiceListForAllAdapter
    lateinit var serviceListForMyAdapter: ServiceListForMyAdapter
    var islogin: Boolean = false
    var isloading: Boolean = false
    var username: String = ""
    var password: String = ""
    var login_cookie: String? = ""

    var server_type = 1//当前显示的服务器类型 1全部 2我的

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ServerList.addItemDecoration(
            MyDividerItemDecoration(
                this@MainActivity,
                RecyclerView.VERTICAL
            )
        )

        RegisterListener()
        initView()
        loadLocaleData()
    }


    private fun RegisterListener() {
        RefreshBtn.setOnClickListener {
            if (islogin){
                getServerList()
            }else{
                login()
            }
        }

        menuBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menu_all ->{
                    server_type=1
                    drawerLayout.closeDrawers()
                    if (this::serviceListForAllAdapter.isInitialized){
                        ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                        ServerList.adapter = serviceListForAllAdapter
                    }
                }
                R.id.menu_me ->{
                    server_type=2
                    drawerLayout.closeDrawers()
                    if (this::serviceListForMyAdapter.isInitialized){
                        ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                        ServerList.adapter = serviceListForMyAdapter
                    }
                }
                R.id.menu_logout ->{
                    drawerLayout.closeDrawers()
                    MMKVHelper.ClearUserInfo()
                    MMKVHelper.ClearAll()
                    initView()
                }
            }
            return@setNavigationItemSelectedListener true
        }

        exitBtn.setOnClickListener {
            finish()
        }

        searchView.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (this@MainActivity::serviceListForAllAdapter.isInitialized){
                    serviceListForAllAdapter.filter.filter(s.toString())
                }
            }
        })

    }


    private fun initView() {
        /***
         * 判断是否存在用户数据
         */
        val userinfo: JSONObject? = MMKVHelper.GetUserInfo()
        if (userinfo == null) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)

            val layoutInflater: LayoutInflater =
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewGroup: ViewGroup =
                layoutInflater.inflate(R.layout.layout_login, null) as ViewGroup

            val whyBtn:Button = viewGroup.findViewById(R.id.whyBtn)
            val SCBtn:Button = viewGroup.findViewById(R.id.SCBtn)
            val ATMEBtn:Button = viewGroup.findViewById(R.id.ATMEBtn)
            val tips:TextView = viewGroup.findViewById(R.id.tips)

            whyBtn.setOnClickListener{
                tips.text=getString(R.string.whylogin)
            }
            SCBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(Constant.GITHUB)
                startActivity(intent)
            }
            ATMEBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(Constant.SINA)
                startActivity(intent)
            }

            alertDialog.setTitle("登陆")
            alertDialog.setView(viewGroup)
            alertDialog.setNegativeButton("登陆", DialogInterface.OnClickListener { dialog, which ->
                username = viewGroup.findViewById<EditText>(R.id.editText_username).text.toString()
                password = viewGroup.findViewById<EditText>(R.id.editText_password).text.toString()
                if (TextUtils.isEmpty(username)){
                    Toast.makeText(this,"请输入用户名",Toast.LENGTH_LONG).show()
                    initView()
                    return@OnClickListener
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_LONG).show()
                    initView()
                    return@OnClickListener
                }
                login()
            })
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("不用了", { dialog, which -> finish() })
            alertDialog.show()
            return
        } else {
            username = userinfo.getString(MMKVHelper.JSON_KEY_USERNAME)
            password = userinfo.getString(MMKVHelper.JSON_KEY_PASSWORD)
            if (TextUtils.isEmpty(username)){
                MMKVHelper.ClearUserInfo()
                initView()
                return
            }
            if (TextUtils.isEmpty(password)){
                MMKVHelper.ClearUserInfo()
                initView()
                return
            }
        }
    }

    /***
     * 加载本地数据
     */
    private fun loadLocaleData(){
        /***
         * 判断是否存在本地数据文件
         */
        val string: String? = MMKVHelper.GetAll()
        if (BuildConfig.DEBUG) {
            Log.d(MainActivity::class.java.simpleName, "$string")
        }
        if (TextUtils.isEmpty(string)) {
            login()
        } else {
            showtoast("使用本地数据")

            var serversBean: ServiceListBean? = null

            try {
                serversBean = Gson().fromJson(string, ServiceListBean::class.java)
                serviceListForAllAdapter =
                    ServiceListForAllAdapter(
                        this@MainActivity,
                        serversBean
                    )
                serviceListForMyAdapter =
                    ServiceListForMyAdapter(
                        this@MainActivity,
                        serversBean
                    )
                serviceListForMyAdapter.setMissionExpandListener { buttonView, isChecked, pos ->
                    missionexpand(buttonView, isChecked, pos)
                }
                serviceListForAllAdapter.setMissionExpandListener { buttonView, isChecked, pos ->
                    missionexpand(buttonView, isChecked, pos)
                }
            }catch (e:Exception){
                e.printStackTrace()
                showtoast("解析本地数据失败")
                MMKVHelper.ClearAll()
                login()
                return
            }

            if (serversBean==null){
                showtoast("解析本地数据失败")
                MMKVHelper.ClearAll()
                login()
                return
            }

            ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
            ServerList.adapter = serviceListForAllAdapter

        }
    }


    /***
     * 执行登陆操作，并获取登陆成功的cookie
     */
    fun login() {
        if (islogin) {
            showtoast("已经是登陆状态，如需重新登陆请重启app")
            return
        }
        if (TextUtils.isEmpty(username)||TextUtils.isEmpty(password)){
            showtoast("用户信息不存在")
            initView()
            return
        }
        GetR.Get<String>("/cn/personal/server/?login=yes")
            .tag(this)
            .params("AUTH_FORM", "Y")
            .params("TYPE", "AUTH")
            .params("backurl", "/cn/personal/server/")
            .params("USER_LOGIN", username)
            .params("USER_PASSWORD", password)
            .execute(object : StringConvert() {
                override fun onStart(request: Request<String, out Request<Any, Request<*, *>>>?) {
                    super.onStart(request)
                    islogin = false
                    showtoast("正在登陆")
                }

                override fun onSuccess(response: Response<String?>) {
                    super.onSuccess(response)
                    val headers: Headers = response.headers()
                    val maps: Map<String, List<String>> = headers.toMultimap()

                    val cookies: List<String>? = maps.get("set-cookie")
                    if (cookies != null) {
                        for (tmp in cookies) {
                            val cStr = tmp.split(";")
                            if (!cStr.isNullOrEmpty()){
                                login_cookie+=cStr[0]+"; "//第一个才是需要的cookie信息
                            }
                            if (tmp.contains("BITRIX_SM_LOGIN")){
                                islogin = true
                            }
                        }
                    }

                    if (islogin){
                        MMKVHelper.SaveUserInfo(username, password)
                        showtoast("登陆成功")
                        getServerList()
                    }else{
                        MMKVHelper.ClearUserInfo()
                        showtoast("登陆失败")
                        initView()
                    }

                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    islogin = false
                    showtoast("登陆失败")
                }
            })
    }


    /****
     * 获取服务器列表数据
     */
    fun getServerList() {
        if (!islogin) {
            showtoast("请登录后操作")
            return
        }
        if (login_cookie.isNullOrEmpty()){
            showtoast("请重新登录")
            initView()
            return
        }
        if (isloading) {
            return
        }
        val request = GetR.Get<String>(Constant.SERVER_LIST_URL + System.currentTimeMillis())

        request.headers("Cookie",login_cookie)

        request.tag(this)
        request.execute(object : StringConvert() {
                override fun onStart(request: Request<String, out Request<Any, Request<*, *>>>?) {
                    super.onStart(request)
                    isloading = true
                    progressBar.visibility = View.VISIBLE
                }

                override fun onFinish() {
                    super.onFinish()
                    isloading = false
                    progressBar.visibility = View.GONE
                }

                override fun onSuccess(response: Response<String?>) {
                    super.onSuccess(response)
                    val json = response.body()

                    val serversBean: ServiceListBean

                    try {
                        Log.d("调试",json)
                        serversBean = Gson().fromJson(json,
                            ServiceListBean::class.java)
                    }catch (e:Exception){
                        e.printStackTrace()
                        showtoast("获取服务器列表失败")
                        return
                    }

                    if (serversBean==null){
                        showtoast("获取服务器列表失败")
                        return
                    }

                    serviceListForAllAdapter =
                        ServiceListForAllAdapter(
                            this@MainActivity,
                            serversBean
                        )
                    serviceListForMyAdapter =
                        ServiceListForMyAdapter(
                            this@MainActivity,
                            serversBean
                        )
                    serviceListForMyAdapter.setMissionExpandListener { buttonView, isChecked, pos ->
                        missionexpand(buttonView, isChecked, pos)
                    }
                    serviceListForAllAdapter.setMissionExpandListener { buttonView, isChecked, pos ->
                        missionexpand(buttonView, isChecked, pos)
                    }

                    if (server_type==1){
                        ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                        ServerList.adapter = serviceListForAllAdapter
                    }else{
                        ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                        ServerList.adapter = serviceListForMyAdapter
                    }

                    MMKVHelper.SaveAll(json!!)//保存列表到本地
                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    showtoast("请求错误:${response?.exception?.localizedMessage}")
                }
            })
    }


    /***
     * 翻译
     */
    private fun translate(sourceStr:String?,listener:(dst:String)->Unit,failListener:()->Unit){
        Log.d("调试","翻译内容"+sourceStr)

        val appid = Constant.baiduFanyiAppid
        val salt = utils.getNumber(10)
        val key = Constant.baiduFanyiAppkey

        val sign = utils.stringToMD5(appid+sourceStr+salt+key)

        val request: GetRequest<String> = OkGo.get(
            "https://fanyi-api.baidu.com/api/trans/vip/translate"+"?q="+sourceStr+"&from=auto"+"&to=zh"+"&appid="+appid+"&salt="+salt+"&sign="+sign
        )

        request.execute(object : StringConvert(){
            override fun onSuccess(response: Response<String?>) {
                super.onSuccess(response)
                try {
                    val json = response.body()
                    val ts = Gson().fromJson(json,baiduTranslateBean::class.java)
                    if (!ts.trans_result.isNullOrEmpty()){
                        listener.invoke(ts.trans_result?.get(0)?.dst.toString())
                    }else{
                        failListener.invoke()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                    failListener.invoke()
                }
            }

            override fun onError(response: Response<String>?) {
                super.onError(response)
                failListener.invoke()
            }
        })


    }


    fun missionexpand(buttonView:CompoundButton,isChecked:Boolean,position:Int){
        if (isChecked){
            val layoutInflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewGroup:ViewGroup=layoutInflater.inflate(R.layout.dialog_mission_desc,null) as ViewGroup

            val richEditor: RichEditor = viewGroup.findViewById<RichEditor>(
                R.id.mission_desc
            )
            richEditor.html=serviceListForAllAdapter.getData().servers.get(position).description

            val alertDialog: android.app.AlertDialog.Builder= android.app.AlertDialog.Builder(this)
            alertDialog.setNegativeButton("关闭") { dialog, which ->
                try {
                    val field = dialog::class.java.getSuperclass()?.getDeclaredField( "mShowing" );
                    field?.setAccessible( true );
                    field?.set( dialog, true ); // false - 使之不能关闭(此为机关所在，其它语句相同)
                } catch ( e:Exception) {e.printStackTrace()}
            }
//            alertDialog.setPositiveButton("翻译"
//            ) { dialog, which ->
//                try
//                {
//                    val field = dialog::class.java.getSuperclass()?.getDeclaredField( "mShowing" );
//                    field?.setAccessible( true );
//                    field?.set( dialog, false ); // false - 使之不能关闭(此为机关所在，其它语句相同)
//                }
//                catch ( e:Exception)
//                {e.printStackTrace()}
//                translate(richEditor.html.replace("<br />","").replace("<br/>",""),{
//                    richEditor.html=it
//                },{
//                    richEditor.html="翻译失败"
//                })
//            }
            alertDialog.setTitle(serviceListForAllAdapter.getData().servers.get(position).missioN_NAME)
            alertDialog.setView(viewGroup)
            alertDialog.show()
            buttonView.isChecked=false
        }
    }


    fun showtoast(string: String) {
        Snackbar.make(coordinator, string, Snackbar.LENGTH_LONG).show()
    }


}
