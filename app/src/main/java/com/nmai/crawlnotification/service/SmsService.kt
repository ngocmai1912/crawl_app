package com.nmai.crawlnotification.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
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
import kotlinx.coroutines.withContext
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

        fun startService(context: Context, bundle: Bundle){
            val smsService : Intent = Intent(context, SmsService::class.java)
            smsService.putExtra("notification",bundle)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // https://stackoverflow.com/questions/45525214/are-there-any-benefits-to-using-context-startforegroundserviceintent-instead-o
                context.startForegroundService(smsService)
            }
            else {
                context.startService(smsService)
            }
            Timber.d("sms visible")
        }

    }
    //ko bao h stop
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val bundle = intent?.getBundleExtra("notification")

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

//        SmsReceiveListener.bindListener(this)
        startForeground(ID_NOTIFICATION_CRAWL, notification)

        if(bundle != null){
            val appName = bundle.getString("app_name")
            val appBundle = bundle.getString("app_bundle")
            val createTime = bundle.getString("create_time")
            val title = bundle.getString("title")
            val content = bundle.getString("content")
            val notificationAPI = NotificationAPI(
                appName!!,
                appBundle!!,
                createTime!!,
                title!!,
                content!!
            )
            GlobalScope.launch(Dispatchers.IO){
                val dao = NotificationDatabase.getInstance(application).notificationDao()
                var notification = Noti(
                    _id = null,
                    appName = appName,
                    appBundle = appBundle,
                    createTime = createTime,
                    title = title,
                    content = content,
                    checkPush = ""
                )
                try {
                    val isSuccess = APIRequest.postNotification(notificationAPI)

                    if (isSuccess == 200) {
                        notification.checkPush = "true"
                        Timber.d("post sms  Success!!")
                    }
                }catch (e: Exception){
                    notification.checkPush = "false"
                    withContext(Dispatchers.Main){
                        APIRequest.postNotificationWithFail(appName,createTime,this@SmsService)
                    }
                    Timber.d("post fail sms server")
                }
                senBroadcastNotification(notification)
                dao.insert(notification)
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

    private fun senBroadcastNotification(notification: Noti){
        val intent = Intent("MessageReceiver")
        intent.putExtra("AppBundle", notification.appBundle)
        intent.putExtra("CreateTime", notification.createTime)
        intent.putExtra("Title", notification.title)
        intent.putExtra("Content", notification.content)
        intent.putExtra("AppName", notification.appName)
        intent.putExtra("CheckPush", notification.checkPush)
        sendBroadcast(intent)
        Timber.d("send toi activity")
    }

}