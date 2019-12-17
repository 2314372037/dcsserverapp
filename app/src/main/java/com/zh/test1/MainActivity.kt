package com.zh.test1

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_login.*
import okhttp3.Headers
import org.jetbrains.anko.editText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    var serviceListForAllAdapter: ServiceListForAllAdapter? = null
    var serviceListForMyAdapter: ServiceListForMyAdapter? = null
    lateinit var serversBean: ServiceListBean
    var islogin: Boolean = false
    var isloading: Boolean = false
    var cookie: String = ""
    var username: String = ""
    var password: String = ""

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
        InitData()
    }


    private fun RegisterListener() {
        RefreshBtn.setOnClickListener {
            if (islogin){
                getdata(cookie)
            }else{
                login()
            }
        }
        bottomNavigationView.run {
            setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_all -> {
                        if (serviceListForAllAdapter != null) {
                            ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                            ServerList.adapter = serviceListForAllAdapter
                            if (serviceListForAllAdapter?.getData()?.servers?.size == 0) {
                                showtoast("没有发现服务器")
                            }
                        }
                    }
                    R.id.menu_me -> {
                        if (serviceListForMyAdapter != null) {
                            ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                            ServerList.adapter = serviceListForMyAdapter
                            if (serviceListForMyAdapter?.getData()?.mY_SERVERS?.size == 0) {
                                showtoast("没有发现服务器")
                            }
                        }
                    }
                }
                return@OnNavigationItemSelectedListener true
            })
        }
        searchView.run {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    when (bottomNavigationView.selectedItemId) {
                        R.id.menu_all -> {
                            if (serviceListForAllAdapter != null) {
                                var sb = ServiceListBean()
                                for (b in serversBean.servers) {
                                    if (b.name.toLowerCase().contains(newText.toString().toLowerCase()) || b.iP_ADDRESS.contains(
                                            newText.toString()
                                        )
                                    ) {
                                        sb.servers.add(b)
                                        sb.mY_SERVERS.add(b)
                                    }
                                }
                                if (TextUtils.isEmpty(newText)) {
                                    sb = serversBean
                                }
                                serviceListForAllAdapter =
                                    ServiceListForAllAdapter(this@MainActivity, sb)
                                ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                                ServerList.adapter = serviceListForAllAdapter
                            }
                        }
                        R.id.menu_me -> {
                            if (serviceListForMyAdapter != null) {
                                var sb = ServiceListBean()
                                for (b in serversBean.mY_SERVERS) {
                                    if (b.name.toLowerCase().contains(newText.toString().toLowerCase()) || b.iP_ADDRESS.contains(
                                            newText.toString()
                                        )
                                    ) {
                                        sb.servers.add(b)
                                        sb.mY_SERVERS.add(b)
                                    }
                                }
                                if (TextUtils.isEmpty(newText)) {
                                    sb = serversBean
                                }
                                serviceListForMyAdapter =
                                    ServiceListForMyAdapter(this@MainActivity, sb)
                                ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                                ServerList.adapter = serviceListForMyAdapter
                            }
                        }
                    }
                    return true
                }
            })
        }

    }


    private fun InitData() {
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
                password = viewGroup.findViewById<EditText>(R.id.editText_username).text.toString()
                if (TextUtils.isEmpty(username)){
                    Toast.makeText(this,"请输入用户名",Toast.LENGTH_LONG).show()
                    InitData()
                    return@OnClickListener
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_LONG).show()
                    InitData()
                    return@OnClickListener
                }
                MMKVHelper.SaveUserInfo(username, password)
                InitData()
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
                InitData()
                return
            }
            if (TextUtils.isEmpty(password)){
                MMKVHelper.ClearUserInfo()
                InitData()
                return
            }
        }


        /***
         * 判断是否存在本地数据文件
         */
        val string: String? = MMKVHelper.GetAll()
        if (BuildConfig.DEBUG) {
            Log.d(MainActivity::class.java.simpleName, "" + string)
        }
        if (TextUtils.isEmpty(string)) {
            login()
        } else {
            showtoast("使用本地数据")
            serversBean = Gson().fromJson(string, ServiceListBean::class.java)
            serviceListForAllAdapter =
                ServiceListForAllAdapter(this@MainActivity, serversBean)
            serviceListForMyAdapter =
                ServiceListForMyAdapter(this@MainActivity, serversBean)
            when (bottomNavigationView.selectedItemId) {
                R.id.menu_all -> {
                    ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                    ServerList.adapter = serviceListForAllAdapter
                }
                R.id.menu_me -> {
                    ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                    ServerList.adapter = serviceListForMyAdapter
                }
            }
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
            return
        }
        GetR.Get<String>("/cn/personal/server/")
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
                    islogin = true
                    showtoast("登陆成功")
                    val headers: Headers = response.headers()
                    val maps: Map<String, List<String>> = headers.toMultimap()

                    val cookies: List<String>? = maps.get("set-cookie")

                    if (cookies != null) {
                        for (c in cookies) {
                            val list = c.split(";")
                            if (list.isNotEmpty()) {
                                cookie += list[0] + "; "
                            }
                        }
                    }

                    getdata(cookie)
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
     * @param cookies 以键值对拼装好的cookie，; 做分割
     */
    fun getdata(cookies: String?) {
        if (!islogin) {
            showtoast("请登录后操作")
            return
        }
        if (TextUtils.isEmpty(cookies)) {
            showtoast("cookie is null！")
            return
        }
        if (isloading) {
            return
        }
        isloading = true
        GetR.Get<ServiceListBean>(Constant.SERVER_LIST_URL + System.currentTimeMillis())
            .tag(this)
            .headers("Cookie", cookies)
            .execute(object : JsonConvert<ServiceListBean>() {
                override fun onStart(request: Request<ServiceListBean, out Request<Any, Request<*, *>>>?) {
                    super.onStart(request)
                    progressBar.visibility = View.VISIBLE
                }

                override fun onSuccess(response: Response<ServiceListBean>?) {
                    super.onSuccess(response)
                    isloading = false
                    progressBar.visibility = View.GONE
                    if (response != null) {
                        //这里还需要进一步判断登陆状态

                        serversBean = response.body()
                        serviceListForAllAdapter =
                            ServiceListForAllAdapter(this@MainActivity, serversBean)
                        serviceListForMyAdapter =
                            ServiceListForMyAdapter(this@MainActivity, serversBean)
                        when (bottomNavigationView.selectedItemId) {
                            R.id.menu_all -> {
                                ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                                ServerList.adapter = serviceListForAllAdapter
                            }
                            R.id.menu_me -> {
                                ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                                ServerList.adapter = serviceListForMyAdapter
                            }
                        }

                        MMKVHelper.SaveAll(Gson().toJson(response.body()).toString())//保存列表到本地
                    } else {
                        islogin = false
                        showtoast("未知错误")
                    }
                }

                override fun onError(response: Response<ServiceListBean>?) {
                    super.onError(response)
                    isloading = false
                    progressBar.visibility = View.GONE
                    showtoast("请求错误:${response!!.exception.localizedMessage}")
                }
            })
    }


    fun showtoast(string: String) {
        Snackbar.make(coordinator, string, Snackbar.LENGTH_LONG).show()
    }


}
