package com.nmai.crawl.service

import android.app.Notification
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.nmai.crawl.post.APIRequest
import com.nmai.crawl.post.NotificationAPI
import com.nmai.crawl.repository.Noti
import com.nmai.crawl.repository.NotificationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class CrawlNotificationService : NotificationListenerService() {
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

        val saveNotification = Noti(
            _id = null,
            appName = applicationName,
            appBundle = appBundle,
            title = title,
            content = content,
            checkPush = "false",
            createTime = postTime.toString()
            )

        GlobalScope.launch(Dispatchers.IO){
            try {
                val isSuccessful =  APIRequest.postNotification(postNotifi)
                if(isSuccessful) {
                    saveNotification.checkPush = "true"
                }
                Timber.d("post thanh cong")
            } catch (e: Exception){
                saveNotification.checkPush = "false"
                Timber.d("post that bai $e")
            }

            val dao = NotificationDatabase.getInstance(application).notificationDao()
            dao.insert(saveNotification)
        }

        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
       Timber.d("remote notification")
    }
}