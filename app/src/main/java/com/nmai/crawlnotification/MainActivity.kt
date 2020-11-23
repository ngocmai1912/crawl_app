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
import com.nmai.crawlnotification.repository.NotificationDao
import com.nmai.crawlnotification.repository.NotificationDatabase
import com.nmai.crawlnotification.service.CrawlNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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

        val dao = NotificationDatabase.getInstance(application).notificationDao()

        //Post data with onClick notification
        val post =  intent.getStringExtra("post_again_notification")
        if(post != null){
            postNotificationToServer(dao, post)
        }

        setContentView(R.layout.activity_main)
        context = this
        val enabledListeners = Settings.Secure.getString(
            this.contentResolver,
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

        adapter.onClick = {time ->
            postNotificationToServer(dao,time)
        }
        registerReceiver(onNotice, IntentFilter("MessageReceiver"))
    }


    private val onNotice = object : BroadcastReceiver(){
        @SuppressLint("SimpleDateFormat")
        override fun onReceive(context: Context, intent: Intent) {

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
        Timber.d("destroy app")
        return super.isDestroyed()
    }

    private fun covertModel(listNotifyData: List<Noti>): List<NotificationData>{
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

    private fun postNotificationToServer(dao: NotificationDao, time: String){
        lifecycleScope.launch(Dispatchers.IO){
            val notificationDB = dao.getNotificationWithTime(time)

            val notificationPost = NotificationAPI(
                notificationDB.appName,
                notificationDB.appBundle,
                notificationDB.createTime,
                notificationDB.title,
                notificationDB.content
            )
            try {
                val isSuccess =  APIRequest.postNotification(notificationPost)
                if (isSuccess == 200) {
                    Timber.d("post notification Success!!")
                    notificationDB.checkPush = "true"
                    dao.update(notificationDB)
                    withContext(Dispatchers.Main){
                        Toast.makeText(context,"Post notification success!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            catch (e : Exception){
                Timber.d("post fail!")
                withContext(Dispatchers.Main){
                    withContext(Dispatchers.Main){
                        Toast.makeText(context,"Post fail!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            val list = dao.getAll()
            val listNoti = covertModel(list)

            withContext(Dispatchers.Main){
                adapter.addAll(listNoti)
            }
        }

    }
}