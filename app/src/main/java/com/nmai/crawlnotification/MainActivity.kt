package com.nmai.crawlnotification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.nmai.crawlnotification.service.SmsListener
import com.nmai.crawlnotification.service.SmsReceiveListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var tv : TextView
    private lateinit var recy : RecyclerView
    private lateinit var adapter : NotificationAdapter
    private lateinit var tvNumberFail : TextView
    companion object{
        var context : Context? = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = NotificationDatabase.getInstance(application).notificationDao()

        //Post data with onClick notification
        val post = intent.getStringExtra("post_again_notification")
        if (post != null) {
            postNotificationToServer(dao, post)
        }

        setContentView(R.layout.activity_main)
        tvNumberFail = findViewById(R.id.number_fail)

        getFailNumber(dao)

        context = this
        // set listener for sms receive
        //SmsReceiveListener.bindListener(this)
        val enabledListeners = Settings.Secure.getString(
            this.contentResolver,
            "enabled_notification_listeners"
        )
        var str = CrawlNotificationService().javaClass.toString()
        if (!enabledListeners.contains(str.subSequence(6, str.length)))
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

        requestSmsPermission()


//        if (!isSmsPermissionGranted())
//            requestReadSmsPermission()
        recy = findViewById(R.id.list_notification)
        adapter = NotificationAdapter(this)
        recy.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val list = dao.getAll()
            val listNoti = covertModel(list)

            withContext(Dispatchers.Main) {
                adapter.addAll(listNoti)
            }
        }

        adapter.onClick = { time ->
            postNotificationToServer(dao, time)
        }
        registerReceiver(onNotice, IntentFilter("MessageReceiver"))

        //gui lai all notify bi fail
        val btnSendAll = findViewById<Button>(R.id.btn_Send_all)
        btnSendAll.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                val list = dao.getNotificationFailPost("false")
                list.forEach {
                    val notificationPost = NotificationAPI(
                        it.appName,
                        it.appBundle,
                        it.createTime,
                        it.title,
                        it.content
                    )

                    try {
                        val isSuccess =  APIRequest.postNotification(notificationPost)
                        if (isSuccess == 200) {
                            Timber.d("post notification Success!!")
                            it.checkPush = "true"
                            dao.update(it)
                        }
                    }
                    catch (e: Exception){
                        Timber.d("post fail!")
                        withContext(Dispatchers.Main){
                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "Post fail!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    getFailNumber(dao)
                }
                val listNew = dao.getAll()
                val listNoti = covertModel(listNew)
                withContext(Dispatchers.Main){
                    adapter.addAll(listNoti)
                }
            }

        }
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
            lifecycleScope.launch(Dispatchers.IO){
                val dao = NotificationDatabase.getInstance(context).notificationDao()
                withContext(Dispatchers.Main){
                    getFailNumber(dao)
                }
            }
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
            val noti = NotificationData(
                it.appName,
                it.appBundle,
                it.createTime,
                it.title,
                it.content,
                isTrue
            )
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
                    val list = dao.getAll()
                    val listNoti = covertModel(list)
                    withContext(Dispatchers.Main){
                        adapter.addAll(listNoti)
                        Toast.makeText(context, "Post notification success!", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            catch (e: Exception){
                Timber.d("post fail!")
                withContext(Dispatchers.Main){
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Post fail!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        getFailNumber(dao)
    }

    private fun getFailNumber(dao:NotificationDao){
        lifecycleScope.launch(Dispatchers.IO){
            val sizeList = dao.getCount()
            withContext(Dispatchers.Main){
                tvNumberFail.text = sizeList.toString()
            }
        }
    }

    // set permission
    private fun requestReadSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_SMS
            )) {
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestSmsPermission() {
        val permission = Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permissionList = arrayOfNulls<String>(1)
            permissionList[0] = permission
            ActivityCompat.requestPermissions(this, permissionList, 1)
        }
    }
}