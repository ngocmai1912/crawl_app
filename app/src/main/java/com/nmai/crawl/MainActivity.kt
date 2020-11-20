package com.nmai.crawl

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nmai.crawl.model.Data
import com.nmai.crawl.model.NotificationData
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var tv : TextView
    private lateinit var recy : RecyclerView
    private lateinit var adapter : NotificationAdapter
    companion object{
        var context : Context? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        val enabledListeners = Settings.Secure.getString(
            this.getContentResolver(),
            "enabled_notification_listeners"
        ).toBoolean()
        if(enabledListeners) startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        recy = findViewById(R.id.list_notification)
        adapter = NotificationAdapter(this,{})
        recy.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        Thread{
            adapter.addAll(Data.getAll())
        }.start()
        adapter.onClick = {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you push notifications to the server?")
                .setPositiveButton("Yes", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {

                    }
                })
            builder.create()
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
            Thread{
                Data.insert(tmp)
            }.start()
        }

    }
}