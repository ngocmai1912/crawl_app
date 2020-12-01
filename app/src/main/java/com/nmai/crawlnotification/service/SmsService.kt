package com.nmai.crawlnotification.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.nmai.crawlnotification.MainActivity
import com.nmai.crawlnotification.post.APIRequest
import com.nmai.crawlnotification.post.NotificationAPI
import com.nmai.crawlnotification.repository.Noti
import com.nmai.crawlnotification.repository.NotificationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class SmsService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return Binder()
    }
    private val CHANNEL_NOTIFICATION_SERVICE = "114"
    private val ID_NOTIFICATION_CRAWL = 114
    private val NAME_CHANNEL = "crawl sms"
    private val DESCRIPTION_TEXT = "push sms to server and save database"

    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        fun startService(context: Context,appName: String, appBundle: String,createTime: String,title: String,content: String){
            val smsService : Intent = Intent(context, SmsService::class.java)
            smsService.putExtra("app_name",appName)
            smsService.putExtra("app_bundle",appBundle)
            smsService.putExtra("create_time",createTime)
            smsService.putExtra("title",title)
            smsService.putExtra("content",content)

            context.startService(smsService)

            Timber.d("sms visible")
        }

        fun stopService(context: Context){
            val smsService : Intent = Intent(context, SmsService::class.java)
            context.stopService(smsService)
        }

    }
    //ko bao h stop
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appName = intent?.getStringExtra("app_name")
        val appBundle = intent?.getStringExtra("app_bundle")
        val createTime = intent?.getStringExtra("create_time")
        val title = intent?.getStringExtra("title")
        val content = intent?.getStringExtra("content")


        createNotificationChanel()
        val pendingIntent : PendingIntent =
            Intent(this, MainActivity::class.java).let { smsIntent ->
                PendingIntent.getActivities(this, 0, arrayOf(smsIntent), 0)
            }

        val notification : Notification =  NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_SERVICE)
            .setContentTitle("App crawl")
            .setContentText("send sms")
            .setContentIntent(pendingIntent)
            .build()

        Timber.d("send sms")

        startForeground(ID_NOTIFICATION_CRAWL, notification)

        GlobalScope.launch(Dispatchers.IO){
            var check = true
            val notificationAPI = NotificationAPI(
                appName,
                MainActivity.PACKAGE_NAME_SMS!!,
                createTime,
                title,
                content
            )

            val dao = NotificationDatabase.getInstance(application).notificationDao()
            var notification = Noti(
                _id = null,
                appName = appName,
                appBundle = MainActivity.PACKAGE_NAME_SMS!!,
                createTime = createTime,
                title = title,
                content = content,
                checkPush = "true"
            )
            try {
                val isSuccess = APIRequest.postNotification(notificationAPI)

                if (isSuccess == 200) {
                    dao.insert(notification)
                    //senBroadcastNotification(notification)
                    Timber.d("post sms  Success!!'")
                }
            }catch (e: Exception){
                notification.checkPush = "false"
                dao.insert(notification)
                //senBroadcastNotification(notification)

                check = false
                Timber.d("post fail sms server")
            }
        }

        val countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Timber.d("Countdown seconds remaining:  ${millisUntilFinished / 1000}" )
            }
            override fun onFinish() {
                stopSelf()
                Timber.d("finish service - stop")
            }

        }

        countDownTimer.start()
        return START_STICKY
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