package com.nmai.crawl.service

import android.app.Notification
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.nmai.crawl.post.NotificationAPI
import java.util.*


class CrawlNotificationService : NotificationListenerService() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        var extras  = sbn.notification.extras
        var appBundle = sbn.packageName
        var postTime = sbn.postTime

        var title = extras.get(Notification.EXTRA_TITLE).toString()
        var content = extras.get(Notification.EXTRA_TEXT).toString()
        val pm = applicationContext.packageManager
        val ai: ApplicationInfo?
        ai = pm.getApplicationInfo(sbn.packageName, 0)
        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String
        val intent = Intent("MessageReceiver")
        intent.putExtra("AppBundle", appBundle)
        intent.putExtra("CreateTime", postTime.toString())
        intent.putExtra("Title", title)
        intent.putExtra("Content", content)
        intent.putExtra("AppName", applicationName)

        val postNotifi = NotificationAPI(applicationName,appBundle,postTime.toString(),title,content)
        startService(postNotifi)
        stopService()
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
       Log.d("remove_notification", "remove_notification")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService(notificationAPI: NotificationAPI){
        val serviceIntent : Intent = Intent(this, ForegroundNotificationService::class.java)
        serviceIntent.putExtra("notification_test", Gson().toJson(notificationAPI))

        startForegroundService(serviceIntent)
        Log.d("check","post data")

    }

    private fun stopService(){
        val serviceIntent : Intent = Intent(this, ForegroundNotificationService::class.java)
        stopService(serviceIntent)
    }
}