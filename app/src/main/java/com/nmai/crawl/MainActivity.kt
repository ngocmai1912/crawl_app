package com.nmai.crawl

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nmai.crawl.model.Data
import com.nmai.crawl.model.NotificationData
import com.nmai.crawl.post.APIRequest
import com.nmai.crawl.post.NotificationAPI
import com.nmai.crawl.repository.Noti
import com.nmai.crawl.repository.NotificationDatabase
import com.nmai.crawl.service.CrawlNotificationService
import com.nmai.crawl.service.ForegroundNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var tv : TextView
    private lateinit var recy : RecyclerView
    private lateinit var adapter : NotificationAdapter
    companion object{
        var context : Context? = null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        val enabledListeners = Settings.Secure.getString(
            this.getContentResolver(),
            "enabled_notification_listeners"
        )
        var str = CrawlNotificationService().javaClass.toString()
        if(!enabledListeners.contains(str.subSequence(6, str.length)))
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));


        recy = findViewById(R.id.list_notification)
        adapter = NotificationAdapter(this)
        recy.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        lifecycleScope.launch(Dispatchers.IO){
            val list = NotificationDatabase.getInstance(application).notificationDao().getAll()
            val listNoti = ArrayList<NotificationData>()
            list.forEach{
                val noti = NotificationData(it.appName,it.appBundle,it.createTime,it.title,it.content,true)
                listNoti.add(noti)
            }

            withContext(Dispatchers.Main){
                adapter.addAll(listNoti)
            }
        }

        adapter.onClick = {
            Log.d("check","test----------------------------")

        }
        registerReceiver(onNotice, IntentFilter("MessageReceiver"))
    }


    private val onNotice = object : BroadcastReceiver(){
        @SuppressLint("SimpleDateFormat")
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("alabama", "hello")

            val postTime = intent.getStringExtra("CreateTime")
            val title = intent.getStringExtra("Title")
            val content = intent.getStringExtra("Content")
            val appName = intent.getStringExtra("AppName")
            val appBundle = intent.getStringExtra("AppBundle")
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss a")
            val date = Date()
            date.time = postTime!!.toLong()
            val createTime = sdf.format(Date())
            val tmp = NotificationData(
                appName!!,
                appBundle!!,
                createTime,
                title!!,
                content!!, false
            )
            adapter.add(tmp)
        }

    }

    override fun isDestroyed(): Boolean {
        Log.d("check", "destroy app--------")
        return super.isDestroyed()
    }
}