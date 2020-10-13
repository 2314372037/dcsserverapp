package com.zh.dcsservertools.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.zh.dcsservertools.R
import com.zh.dcsservertools.adapter.ServiceListForAllAdapter
import com.zh.dcsservertools.adapter.ServiceListForMyAdapter
import com.zh.dcsservertools.bean.ServiceListBean
import com.zh.dcsservertools.bean.baiduTranslateBean
import com.zh.dcsservertools.helper.MMKVHelper
import com.zh.dcsservertools.helper.MyDividerItemDecoration
import com.zh.dcsservertools.helper.NetworkHelper
import com.zh.dcsservertools.helper.Utils
import com.zh.dcsservertools.ui.widget.MyWebView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    val net by lazy { NetworkHelper(this) }
    var loadingDialog: AlertDialog? = null
    var serviceListForAllAdapter: ServiceListForAllAdapter? = null
    var serviceListForMyAdapter: ServiceListForMyAdapter? = null
    var islogin: Boolean = false
    var isloading: Boolean = false
    var username: String = ""
    var password: String = ""
    var login_cookie: String? = ""

    var server_type = 1//当前显示的服务器类型 1全部 2我的

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initAdapter()
        initData()
    }

    private fun initView() {
        initSearchView(toolBar.menu)
        toolBar.setOnMenuItemClickListener {
            it?.let {
                when (it.itemId) {
                    R.id.itChanged -> {
                        if (server_type == 1) {
                            server_type = 2
                            it.setTitle("全部服务器")
                            serviceListForMyAdapter?.let {
                                ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                                ServerList.adapter = it
                                toolBar.title =
                                    "游戏服务器（数量${serviceListForMyAdapter?.getData()?.mY_SERVERS?.size}）"
                            }
                        } else {
                            server_type = 1
                            it.setTitle("我的服务器")
                            serviceListForAllAdapter?.let {
                                ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                                ServerList.adapter = it
                                toolBar.title =
                                    "游戏服务器（数量${serviceListForAllAdapter?.getData()?.servers?.size}）"
                            }
                        }
                    }
                    R.id.itExitLogin -> {
                        MMKVHelper.ClearUserInfo()
                        MMKVHelper.ClearAll()
                        showLogin()
                    }
                    else -> {
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
        actionButton.setOnClickListener {
            if (islogin) {
                getServerList()
            } else {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    showLoading()
                    login({
                        showtoast("登录成功")
                        getServerList()
                        dismissLoading()
                    }, {
                        showtoast("登录失败")
                        showLogin()
                        dismissLoading()
                    })
                } else {
                    showLogin()
                }
            }
        }
    }

    private fun initSearchView(menu: Menu) {
        val menuItem = menu.findItem(R.id.searchBox)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = "输入服务器名称或ip"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (server_type == 1) {
                        if (serviceListForAllAdapter != null) {
                            serviceListForAllAdapter?.filter?.filter(it)
                        }
                    } else if (server_type == 2) {
                        if (serviceListForMyAdapter != null) {
                            serviceListForMyAdapter?.filter?.filter(it)
                        }
                    }
                }
                return true
            }
        })

        try {
            //反射获取SearchAutoComplete mSearchSrcTextView，修改TextHint颜色
            val tmpClass = searchView::class.java
            val tmpField = tmpClass.getDeclaredField("mSearchSrcTextView")
            tmpField.isAccessible = true//设置可访问
            val tmpObj = tmpField.get(searchView)
            if (tmpObj is SearchView.SearchAutoComplete) {
                tmpObj.setTextColor(Color.WHITE)
                tmpObj.setHintTextColor(Color.WHITE)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun initAdapter() {
        ServerList.addItemDecoration(
            MyDividerItemDecoration(
                this@MainActivity,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun showTip() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(false)
        alertDialog.setTitle("温馨提示")
        val view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_tip, null)
        view.findViewById<MyWebView>(R.id.webView)?.loadData(getString(R.string.whylogin))
        alertDialog.setView(view)
        alertDialog.setNegativeButton("不接受") { d, i ->
            finish()
        }
        alertDialog.setPositiveButton("接受") { d, i ->
            showLogin()
        }
        alertDialog.show()
    }

    private fun showLogin() {
        val loginAlertDialog = AlertDialog.Builder(this)
        loginAlertDialog.setCancelable(false)
        loginAlertDialog.setTitle("请先登录:)")
        val view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_login, null)
        view.findViewById<TextView>(R.id.tvGITHUB)?.setOnClickListener {
            //从其他浏览器打开
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val content_url = Uri.parse(NetworkHelper.GITHUB)
            intent.data = content_url
            startActivity(Intent.createChooser(intent, "请选择浏览器"))
        }
        view.findViewById<TextView>(R.id.tvWB)?.setOnClickListener {
            //从其他浏览器打开
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val content_url = Uri.parse(NetworkHelper.SINA)
            intent.data = content_url
            startActivity(Intent.createChooser(intent, "请选择浏览器"))
        }


        loginAlertDialog.setView(view)
        loginAlertDialog.setNegativeButton("退出") { d, i ->
            finish()
        }
        loginAlertDialog.setPositiveButton("登录") { d, i ->
            username = view.findViewById<EditText>(R.id.evUsername).text.toString()
            password = view.findViewById<EditText>(R.id.evPassword).text.toString()
            if (username.isEmpty()) {
                showtoast("请输入用户名")
                showLogin()
                return@setPositiveButton
            }
            if (password.isEmpty()) {
                showtoast("请输入密码")
                showLogin()
                return@setPositiveButton
            }
            showLoading("登录中")
            login({
                showtoast("登录成功")
                getServerList()
                dismissLoading()
            }, {
                showtoast("登录失败")
                showLogin()
                dismissLoading()
            })
        }
        loginAlertDialog.show()
    }

    private fun initData() {
        //判断是否存在用户数据
        val userinfo: JSONObject? = MMKVHelper.GetUserInfo()
        if (userinfo == null) {
            showTip()
            return
        } else {
            username = userinfo.getString(MMKVHelper.JSON_KEY_USERNAME)
            password = userinfo.getString(MMKVHelper.JSON_KEY_PASSWORD)
            if (TextUtils.isEmpty(username)) {
                MMKVHelper.ClearUserInfo()
                showLogin()
                return
            }
            if (TextUtils.isEmpty(password)) {
                MMKVHelper.ClearUserInfo()
                showLogin()
                return
            }
            loadLocaleData()
        }
    }

    /***
     * 加载本地数据
     */
    private fun loadLocaleData() {
        val string: String? = MMKVHelper.GetAll()
        if (string.isNullOrEmpty()) {
            showLoading("登录中")
            login({
                showtoast("登录成功")
                getServerList()
                dismissLoading()
            }, {
                showtoast("登录失败")
                dismissLoading()
            })
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
                serviceListForMyAdapter?.setMissionExpandListener { pos ->
                    missionexpand(pos)
                }
                serviceListForAllAdapter?.setMissionExpandListener { pos ->
                    missionexpand(pos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showtoast("解析本地数据失败")
                MMKVHelper.ClearAll()
                MMKVHelper.ClearUserInfo()
                initView()
                return
            }
            ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
            ServerList.adapter = serviceListForAllAdapter
            toolBar.title =
                "游戏服务器（数量${serviceListForAllAdapter?.getData()?.servers?.size}）"
        }
    }


    /***
     * 执行登陆操作，并获取登陆成功的cookie
     */
    fun login(sucessListener: () -> Unit, failListener: (msg: String) -> Unit) {
        if (islogin) {
            showtoast("已经是登陆状态，无需登录")
            return
        }

        net.response(
            "/cn/auth/?login=yes",
            null,
            hashMapOf(
                Pair("AUTH_FORM", "Y"),
                Pair("TYPE", "AUTH"),
//                Pair("backurl", "/cn/auth"),//注意加上这个会返回Location，貌似httpUrlConnection会自动重定向，导致无法登录成功
                Pair("USER_LOGIN", "$username"),
                Pair("USER_PASSWORD", "$password")
            ),
            { data, map ->
                val cookies: List<String>? = map.get("set-cookie")
                if (cookies != null) {
                    for (tmp in cookies) {
                        val cStr = tmp.split(";")
                        if (!cStr.isNullOrEmpty()) {
                            login_cookie += cStr[0] + "; "//第一个才是需要的cookie信息
                            if (cStr[0].contains("BITRIX_SM_LOGIN")) {
                                islogin = true
                            }
                        }
                    }
                }

                if (islogin) {
                    MMKVHelper.SaveUserInfo(username, password)
                    sucessListener.invoke()
                } else {
                    MMKVHelper.ClearUserInfo()
                    failListener.invoke("")
                }
            },
            { code, msg ->
                failListener.invoke(msg)
            }, "POST"
        )
    }

    /***
     * 格式化cookie，取出必要信息
     * 注意这里只有某些差参数是必要的
     * 例子数据PHPSESSID=r7QZ7f7q02iaqbo6nlj3Y2Iccccc; BITRIX_SM_SALE_UID=333333; BITRIX_SM_NCC=Y; BITRIX_SM_LOGIN=22222222; BITRIX_SM_SOUND_LOGIN_PLAYED=Y; BITRIX_SM_GUEST_ID=60550448;
    BITRIX_SM_LAST_VISIT=07.08.2020+09%3A32%3A31;
     */
    private fun formatCookie(cookie: String?): String {
        var cookieStr = ""
        try {
            cookie?.split(";")?.let {
                for (i in it.indices) {
                    if (i == 0) {//
                        if (it.get(i).contains("PHPSESSID")) {
                            continue
                        }
                    }
                    cookieStr += it.get(i) + "; "
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return cookieStr
    }

    /****
     * 获取服务器列表数据
     */
    fun getServerList() {
        if (!islogin) {
            showtoast("请登录后操作")
            return
        }
        if (login_cookie.isNullOrEmpty()) {
            showtoast("cookie为空请重新登录")
            return
        }
        if (isloading) {
            return
        }
        Log.d("调试", "格式化前cookie->$login_cookie")
        Log.d("调试", "格式化后cookie->${formatCookie(login_cookie)}")
        isloading = true
        progressBar.visibility = View.VISIBLE
        net.response(
            NetworkHelper.SERVER_LIST_URL + System.currentTimeMillis(),
            hashMapOf(Pair("Cookie", formatCookie(login_cookie))),
            null,
            { data, map ->
                val serversBean: ServiceListBean

                try {
                    serversBean = Gson().fromJson(data, ServiceListBean::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showtoast("获取服务器列表失败(请尝试重新打开app)")
                    progressBar.visibility = View.GONE
                    return@response
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
                serviceListForMyAdapter?.setMissionExpandListener { pos ->
                    missionexpand(pos)
                }
                serviceListForAllAdapter?.setMissionExpandListener { pos ->
                    missionexpand(pos)
                }

                if (server_type == 1) {
                    ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                    ServerList.adapter = serviceListForAllAdapter
                    toolBar.title =
                        "游戏服务器（数量${serviceListForAllAdapter?.getData()?.servers?.size}）"
                } else {
                    ServerList.layoutManager = LinearLayoutManager(this@MainActivity)
                    ServerList.adapter = serviceListForMyAdapter
                    toolBar.title =
                        "游戏服务器（数量${serviceListForAllAdapter?.getData()?.mY_SERVERS?.size}）"
                }

                MMKVHelper.SaveAll(data)//保存列表到本地

                isloading = false
                progressBar.visibility = View.GONE
            },
            { code, msg ->
                showtoast("请求异常:${msg}")
                isloading = false
                progressBar.visibility = View.GONE
            })
    }


    /***
     * 翻译
     */
    private fun translate(
        sourceStr: String?,
        listener: (dst: String) -> Unit,
        failListener: () -> Unit
    ) {
        Log.d("调试", "翻译内容" + sourceStr)

        val appid = NetworkHelper.baiduFanyiAppid
        val salt = Utils.getNumber(10)
        val key = NetworkHelper.baiduFanyiAppkey

        val sign = Utils.stringToMD5(appid + sourceStr + salt + key)

        net.response(
            "https://fanyi-api.baidu.com/api/trans/vip/translate?q=$sourceStr&from=auto&to=zh&appid=$appid&salt=$salt&sign=$sign",
            null,
            null,
            { data, map ->
                try {
                    val ts = Gson().fromJson(data, baiduTranslateBean::class.java)
                    if (!ts.trans_result.isNullOrEmpty()) {
                        listener.invoke(ts.trans_result?.get(0)?.dst.toString())
                    } else {
                        failListener.invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    failListener.invoke()
                }
            },
            { code, msg ->
                failListener.invoke()
            })
    }


    fun missionexpand(position: Int) {
        val layoutInflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewGroup: ViewGroup =
            layoutInflater.inflate(R.layout.layout_dialog_tip, null) as ViewGroup

        viewGroup.findViewById<MyWebView>(R.id.webView)
            ?.loadData(serviceListForAllAdapter!!.getData().servers.get(position).description)

        val alertDialog: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        alertDialog.setNegativeButton("关闭") { dialog, which ->
            try {
                val field = dialog::class.java.getSuperclass()?.getDeclaredField("mShowing");
                field?.setAccessible(true);
                field?.set(dialog, true); // false - 使之不能关闭(此为机关所在，其它语句相同)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        alertDialog.setTitle(serviceListForAllAdapter!!.getData().servers.get(position).missioN_NAME)
        alertDialog.setView(viewGroup)
        alertDialog.show()
    }


    fun showtoast(string: String) {
//        Snackbar.make(coordinator, string, Snackbar.LENGTH_LONG).show()
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }

    fun showLoading(title: String = "请稍等") {
        if (loadingDialog == null) {
            val progressBar = ProgressBar(this)
            loadingDialog = AlertDialog.Builder(this).create()
            loadingDialog?.setCancelable(false)
            loadingDialog?.setView(progressBar, 0, 0, 0, 50)
        }
        loadingDialog?.setMessage(title)
        loadingDialog?.show()
    }

    fun dismissLoading() {
        loadingDialog?.dismiss()
    }

}
