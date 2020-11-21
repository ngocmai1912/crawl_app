package com.nmai.crawlnotification

import android.annotation.SuppressLint
import android.content.*
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
import com.nmai.crawlnotification.model.NotificationData
import com.nmai.crawlnotification.post.APIRequest
import com.nmai.crawlnotification.post.NotificationAPI
import com.nmai.crawlnotification.repository.Noti
import com.nmai.crawlnotification.repository.NotificationDatabase
import com.nmai.crawlnotification.service.CrawlNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        val dao = NotificationDatabase.getInstance(application).notificationDao()

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
            val list = dao.getAll()
            val listNoti = covertModel(list)

            withContext(Dispatchers.Main){
                adapter.addAll(listNoti)
            }
        }

        adapter.onClick = {
            lifecycleScope.launch(Dispatchers.IO){
                val list = dao.getNotificationFailPost("false")
                list.forEach{
                    val notificationAPI = NotificationAPI(it.appBundle,it.createTime,it.title,it.content)
                    val isSuccessful = APIRequest.postNotification(notificationAPI)
                    if (isSuccessful == 200){
                        it.checkPush = "true"
                        dao.insert(it)

                     //   Timber.d("POST notification ${it._id} thanh cong")
                    }
                }
            }
            Toast.makeText(this,"Push Notification!!",Toast.LENGTH_SHORT).show()

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
            val checkPush = intent.getStringExtra("CheckPush")

            var check = true
            if(checkPush == "false") check = false
            val tmp = NotificationData(
                appName!!,
                appBundle!!,
                postTime!!.toString(),
                title!!,
                content!!, check
            )
            adapter.add(tmp)
        }

    }

    override fun isDestroyed(): Boolean {
        //Timber.d("destroy app")
        return super.isDestroyed()
    }

    fun covertModel(listNotifyData: List<Noti>): List<NotificationData>{
        val list = ArrayList<NotificationData>()
        listNotifyData.forEach {
            var isTrue = true
            if(it.checkPush == "true") isTrue = true
            else if(it.checkPush == "false") isTrue = false
            val noti = NotificationData(it.appName,it.appBundle,it.createTime,it.title,it.content,isTrue)
            list.add(noti)
        }

        return list
    }
}