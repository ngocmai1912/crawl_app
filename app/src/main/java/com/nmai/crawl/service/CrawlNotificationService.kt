package com.nmai.crawl.service

import android.app.Notification
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.icu.text.SimpleDateFormat
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.*


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
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
       Log.d("remove_notification", "remove_notification")
    }
}