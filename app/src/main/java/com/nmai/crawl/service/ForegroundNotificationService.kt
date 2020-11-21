package com.nmai.crawl.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.nmai.crawl.MainActivity
import com.nmai.crawl.post.APIRequest
import com.nmai.crawl.post.NotificationAPI
import com.nmai.crawl.repository.Noti
import com.nmai.crawl.repository.NotificationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForegroundNotificationService : Service(){
    override fun onBind(p0: Intent?): IBinder? {
        return Binder()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationDB = intent?.getStringExtra("notification_test")
        val testNotifi = Gson().fromJson(notificationDB,NotificationAPI::class.java)

        Log.d("check",testNotifi.toString())

        GlobalScope.launch(Dispatchers.IO){
            if (notificationDB !=null){
                val notificationDao = NotificationDatabase.getInstance(application).notificationDao()
                val notificationDb: Noti? = notificationDao.getNotificationWithContent(testNotifi.content)
                if(notificationDb != null ){
                    val checkNotification = APIRequest. transformNotification(notificationDb)
                    if(checkNotification != testNotifi) {
                        val check = APIRequest.postNotification(testNotifi)

                        Log.d("check", "on Service $check")
                    }
                }
            }
        }

        createNotificationChanel()
        val pendingIntent : PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivities(this, 0, arrayOf(notificationIntent), 0)
            }

        val notification : Notification =  Notification.Builder(this, "113")
            .setContentTitle("ds")
            .setContentText("ds")
            .setContentIntent(pendingIntent)
            .setTicker("ds")
            .build()


        startForeground(113, notification)

        return START_NOT_STICKY

    }
    private fun createNotificationChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "post notification"
            val descriptionText = "descriptionText"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("113", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}