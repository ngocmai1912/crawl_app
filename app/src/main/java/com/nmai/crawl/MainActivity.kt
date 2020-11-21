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
import com.nmai.crawl.service.ForegroundNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


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
        Thread{
            adapter.addAll(Data.getAll())
        }.start()
        adapter.onClick = {
//            val builder = AlertDialog.Builder(context)
//            builder.setMessage("Do you push notifications to the server?")
//                .setPositiveButton("Yes", object : DialogInterface.OnClickListener{
//                    override fun onClick(p0: DialogInterface?, p1: Int) {
//                        Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show()
//                    }
//                })
//                .setNegativeButton("No", object : DialogInterface.OnClickListener {
//                    override fun onClick(p0: DialogInterface?, p1: Int) {
//
//                    }
//                })
//            builder.create()
            Log.d("check","test----------------------------")

        }
        registerReceiver(onNotice, IntentFilter("MessageReceiver"))
    }


    private val onNotice = object : BroadcastReceiver(){
        @RequiresApi(Build.VERSION_CODES.O)
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
            Thread{
                Data.insert(tmp)
            }.start()
        }

    }
}