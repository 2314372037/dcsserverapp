package com.zh.dcsservertools.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.zh.dcsservertools.R
import com.zh.dcsservertools.bean.ServiceListBean
import jp.wasabeef.richeditor.RichEditor
import org.jetbrains.anko.textColor

class ServiceListForMyAdapter(
    private val activity: Activity, bean: ServiceListBean
) : RecyclerView.Adapter<ServiceListForMyAdapter.MyViewHolder>() , Filterable {

    private var sourceServiceListBean: ServiceListBean =
        ServiceListBean()
    private var serviceListBean: ServiceListBean =
        ServiceListBean()

    private lateinit var missionExpandListener:(buttonView:CompoundButton,isChecked:Boolean,pos:Int)->Unit

    init {
        sourceServiceListBean=bean
        serviceListBean=bean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater: LayoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.server_list_item, parent, false)
        return MyViewHolder(
            view
        )
    }

    fun setMissionExpandListener(listener:(buttonView:CompoundButton,isChecked:Boolean,pos:Int)->Unit){
        this.missionExpandListener=listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = serviceListBean.mY_SERVERS?.get(position)?.name
        holder.ip.text = "ip地址:${serviceListBean.mY_SERVERS?.get(position)?.iP_ADDRESS}"
        holder.port.text = "端口:${serviceListBean.mY_SERVERS?.get(position)?.port}"
        holder.peopleCount.text =
            "人数${serviceListBean.mY_SERVERS?.get(position)?.players}/${serviceListBean.mY_SERVERS?.get(
                position
            )?.playerS_MAX}"
        holder.password.text = "密码:${serviceListBean.mY_SERVERS?.get(position)?.password}"
        holder.missionName.text = serviceListBean.mY_SERVERS?.get(position)?.missioN_NAME
        holder.missionDate.text = "任务时间:${serviceListBean.mY_SERVERS?.get(position)?.missioN_TIME_FORMATTED}"

        if (serviceListBean.mY_SERVERS?.get(position)?.password.equals("是")) {
            holder.password.textColor = activity.getColor(R.color.itemPsd1)
        } else {
            holder.password.textColor = activity.getColor(R.color.itemPsd2)
        }

        if (TextUtils.isEmpty(serviceListBean.mY_SERVERS?.get(position)?.description)||serviceListBean.mY_SERVERS?.get(position)?.description.equals("否")){
            holder.missionExpand.visibility=View.GONE
        }else{
            holder.missionExpand.visibility=View.VISIBLE
            holder.missionExpand.setOnCheckedChangeListener { buttonView, isChecked ->
                if (this::missionExpandListener.isInitialized){
                    missionExpandListener.invoke(buttonView,isChecked,position)
                }
            }
        }

        holder.share.setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, serviceListBean.servers?.get(position)?.name+serviceListBean.servers?.get(position)?.iP_ADDRESS+":"+serviceListBean.servers?.get(position)?.port)
            intent.type = "text/plain"
            activity.startActivity(Intent.createChooser(intent,"选择分享应用"))
        })

    }

    fun addData(postion: Int, bean: ServiceListBean.SERVERSBean) {
        serviceListBean.mY_SERVERS.add(postion, bean)
        notifyItemInserted(postion)
    }

    fun removeData(postion: Int) {
        serviceListBean.mY_SERVERS.removeAt(postion)
        notifyItemRemoved(postion)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return serviceListBean.mY_SERVERS!!.size
    }

    fun getData() : ServiceListBean {
        return serviceListBean
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.server_item_title)
        var ip: TextView = itemView.findViewById(R.id.server_item_ip)
        var port: TextView = itemView.findViewById(R.id.server_item_port)
        var peopleCount: TextView = itemView.findViewById(R.id.server_item_people_cout)
        var password: TextView = itemView.findViewById(R.id.server_item_psd)
        var missionName: TextView = itemView.findViewById(R.id.server_item_mission_name)
        var missionExpand: CheckBox = itemView.findViewById(R.id.server_item_mission_expand)
        var missionDate:TextView = itemView.findViewById(R.id.server_item_date)
        var share: ImageView = itemView.findViewById(R.id.server_item_share)
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val s = constraint.toString()
                if (s.isEmpty()){
                    serviceListBean=sourceServiceListBean
                }else{
                    val tmp = ServiceListBean()
                    for (i in sourceServiceListBean.mY_SERVERS){
                        if (i.name!=null){
                            if (i.name.contains(s)){
                                tmp.mY_SERVERS.add(i)
                            }
                        }
                        if (i.iP_ADDRESS!=null){
                            if (i.iP_ADDRESS.contains(s)){
                                tmp.mY_SERVERS.add(i)
                            }
                        }
                    }
                    serviceListBean=tmp
                }

                val filterResults = FilterResults()
                filterResults.values=serviceListBean
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values!= null){
                    serviceListBean = results.values as ServiceListBean
                    notifyDataSetChanged()
                }
            }
        }
    }

}