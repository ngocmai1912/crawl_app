package com.nmai.crawlnotification.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.nmai.crawlnotification.MainActivity
import com.nmai.crawlnotification.model.NotificationData
import com.nmai.crawlnotification.post.APIRequest
import com.nmai.crawlnotification.post.NotificationAPI
import com.nmai.crawlnotification.repository.Noti
import com.nmai.crawlnotification.repository.NotificationDao
import com.nmai.crawlnotification.repository.NotificationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class CrawlNotificationService : NotificationListenerService(), SmsListener {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        var extras  = sbn.notification.extras
        var appBundle = sbn.packageName
        var postTime = sbn.postTime

        var title = extras.get(Notification.EXTRA_TITLE).toString()
        var content = extras.get(Notification.EXTRA_TEXT).toString()
        val pm = applicationContext.packageManager
        val ai: ApplicationInfo?
        ai = pm.getApplicationInfo(sbn.packageName, 0)



        val nameAppCrawl = getNameApp(this)

        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String

        if (nameAppCrawl != applicationName && appBundle != MainActivity.PACKAGE_NAME_SMS ){
            val postNotifi = NotificationAPI(
                applicationName,
                appBundle,
                postTime.toString(),
                title,
                content
            )
            var saveNotification = Noti(
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
                    if(isSuccessful == 200) {
                        saveNotification.checkPush = "true"
                    }
                    Timber.d("post thanh cong")
                } catch (e: Exception){
                    saveNotification.checkPush = "false"

                    withContext(Dispatchers.Main){
                        APIRequest.postNotificationWithFail(applicationName,postTime.toString(),this@CrawlNotificationService)
                    }
                    Timber.d("post that bai $e")
                }

                val dao = NotificationDatabase.getInstance(application).notificationDao()
                dao.insert(saveNotification)
                senBroadcastNotification(saveNotification)
            }
        }

        SmsReceiveListener.bindListener(this)
    }

    private fun getNameApp(context: Context): String{
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId === 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
            stringId
        )
    }

    override fun messageReceived(
        appName: String,
        appBundle: String,
        createTime: String,
        title: String,
        content: String
    ) {
        val defaultApplication = Settings.Secure.getString(
            contentResolver,
            "sms_default_application"
        )
        var check = true
        val notificationAPI = NotificationAPI(appName, MainActivity.PACKAGE_NAME_SMS!!,createTime,title,content)
        GlobalScope.launch(Dispatchers.IO){
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
                    senBroadcastNotification(notification)
                    Timber.d("post sms  Success!!")
                }
            }catch (e:Exception){
                notification.checkPush = "false"
                dao.insert(notification)
                senBroadcastNotification(notification)

                check = false
                Timber.d("post fail sms server")
            }
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