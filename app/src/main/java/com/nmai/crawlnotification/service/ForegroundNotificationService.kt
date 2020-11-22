package com.nmai.crawlnotification.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.nmai.crawlnotification.MainActivity
import com.nmai.crawlnotification.post.APIRequest
import com.nmai.crawlnotification.post.NotificationAPI
import com.nmai.crawlnotification.repository.Noti
import com.nmai.crawlnotification.repository.NotificationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ForegroundNotificationService : Service(){

    companion object {
        const val CHANNEL_NOTIFICATION_SERVICE = "113"
        const val ID_NOTIFICATION_CRAWL = 113
        const val NAME_CHANNEL = "crawl notification"
        const val DESCRIPTION_TEXT = "push notification to server and save database"


        /**
         * @param nameApp: Truyen ten app de vao trong noi dung
         * @param context: ...
         *
         * */
        fun startService(nameApp: String, context: Context){
            val notificationService : Intent = Intent(context,ForegroundNotificationService::class.java)
            notificationService.putExtra("name_app",nameApp)
            context.startService(notificationService)

            Timber.d("notification visible")
        }

        /**
         * @param context dua 1 context de check xem stop
         * */
        fun stopService(context: Context){
            val notificationService : Intent = Intent(context,ForegroundNotificationService::class.java)

            context.stopService(notificationService)
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return Binder()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val appName = intent?.getStringExtra("app_name")

        createNotificationChanel()
        val pendingIntent : PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivities(this, 0, arrayOf(notificationIntent), 0)
            }

        val notification : Notification =  Notification.Builder(this, CHANNEL_NOTIFICATION_SERVICE)
            .setContentTitle("App crawl")
            .setContentText(appName)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(ID_NOTIFICATION_CRAWL, notification)
        Timber.d("start notification $appName")

        return START_NOT_STICKY

    }

    private fun createNotificationChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel =
                NotificationChannel(CHANNEL_NOTIFICATION_SERVICE, NAME_CHANNEL, importance).apply {
                    description = DESCRIPTION_TEXT
                }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}