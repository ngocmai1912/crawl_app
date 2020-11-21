package com.nmai.crawl

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.nmai.crawl.model.NotificationData
import org.w3c.dom.Text

class NotificationAdapter(
    val context: Context,
) : RecyclerView.Adapter<NotificationAdapter.NotificationHolder>() {
    lateinit var onClick : () -> Unit
    var listNotification = mutableListOf<NotificationData>()
    inner class NotificationHolder(val view : View) : RecyclerView.ViewHolder(view){
        private var tvAppName : TextView = view.findViewById(R.id.tv_app_name)
        private var tvTitle : TextView = view.findViewById(R.id.tv_title)
        private var tvContent : TextView = view.findViewById(R.id.content)
        private var tvCreateTime : TextView = view.findViewById(R.id.create_time)
        private var checkPush : TextView = view.findViewById(R.id.check_push)
        fun bind(notificationData: NotificationData){
            Log.d("akjakja", "aaaa")
            tvAppName.text = notificationData.appName
            tvTitle.text = notificationData.title
            tvContent.text = notificationData.content
            tvCreateTime.text = notificationData.createTime
            if(!notificationData.checkPush) checkPush.text = "Failed"

            itemView.setOnClickListener {
                onClick()
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
        Log.d("aaaassssss", listNotification.toString())
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