package com.nmai.crawlnotification

import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.nmai.crawlnotification.model.NotificationData
import java.util.*

class NotificationAdapter(
    val context: Context
) : RecyclerView.Adapter<NotificationAdapter.NotificationHolder>() {
    lateinit var onClick : (time: String) -> Unit
    var listNotification = mutableListOf<NotificationData>()
    inner class NotificationHolder(val view : View) : RecyclerView.ViewHolder(view){
        private var tvAppName : TextView = view.findViewById(R.id.tv_app_name)
        private var tvTitle : TextView = view.findViewById(R.id.tv_title)
        private var tvContent : TextView = view.findViewById(R.id.content)
        private var tvCreateTime : TextView = view.findViewById(R.id.create_time)
        private var checkPush : TextView = view.findViewById(R.id.check_push)
        private var im : ImageView = view.findViewById(R.id.image)
        fun bind(notificationData: NotificationData){
            tvAppName.text = notificationData.appName
            tvTitle.text = notificationData.title
            tvContent.text = notificationData.content

            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss a")
            val date = Date()
            date.time = notificationData.createTime.toLong()
            val createTime = sdf.format(Date())
            tvCreateTime.text = createTime

            if(!notificationData.checkPush){
                checkPush.text = "Failed"
                checkPush.setTextColor(Color.RED)
                im.setBackgroundResource(R.drawable.ic_fail)
            }
            else{
                checkPush.text = "Successfully"
                checkPush.setTextColor(Color.GREEN)
                im.setBackgroundResource(R.drawable.ic_success)
            }
            itemView.setOnClickListener {
                if(!notificationData.checkPush){
                    onClick(notificationData.createTime)
                }
                else{
                    Toast.makeText(context, "Notification is posted!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun addAll(list : List<NotificationData>){
        listNotification.clear()
        listNotification.addAll(list)
        notifyDataSetChanged()
    }

    fun add(notificationData: NotificationData){
        listNotification.add(notificationData)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent,false)
        return NotificationHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        return holder.bind(listNotification[listNotification.size-1- position])
    }

    override fun getItemCount(): Int {
        return listNotification.size
    }
}