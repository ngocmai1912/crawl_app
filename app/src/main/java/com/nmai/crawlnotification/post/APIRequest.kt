package com.nmai.crawlnotification.post

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nmai.crawlnotification.R
import com.nmai.crawlnotification.repository.Noti
import com.nmai.crawlnotification.service.ForegroundNotificationService
import okhttp3.*
import timber.log.Timber

class APIRequest {
    companion object {

        //Read
        private const val URL = "https://bepay2.com"
        private const val URL_NOTIFICATION = "/api/v2/notification/send"

        //Test
        private const val PORT_TEST = "8083"
        private const val URL_TEST= "http://103.141.140.189:$PORT_TEST"
        private const val URL_NOTIFICATION_TEST = "/notify"

        // Phuong thuc
        private const val POST = "POST"
        private const val GET = "GET"
        private val mediaType: MediaType? = MediaType.parse("application/json")

        //Notification
        private const val CHANNEL_NOTIFICATION_SERVICE = "113"
        private const val ID_NOTIFICATION_CRAWL = 113
        private const val NAME_CHANNEL = "crawl notification"
        private const val DESCRIPTION_TEXT = "push notification to server and save database"

        /**
         * @return list notification
         * */
        fun getAllNotification(): List<NotificationAPI> {
            val client = OkHttpClient().newBuilder()
                .build()
            val request: Request = Request.Builder()
                .url(URL + URL_NOTIFICATION)
                .method("GET", null)
                .build()

            val response: Response = client.newCall(request).execute()
            val json = response.body()?.string()
            //using TypeToken de tra ve
            return Gson().fromJson(json, object : TypeToken<List<NotificationAPI?>?>() {}.type)
        }

        /**
         * @param notificationLog post notification
         * @return  True is post successfull
         * @return False is not post
         * */
        fun postNotification(notificationAPI: NotificationAPI): Int {
            val client = OkHttpClient().newBuilder()
                .build()
            val notificationToJson = Gson().toJson(notificationAPI)
            val body = RequestBody.create(mediaType, notificationToJson)
            val request: Request = Request.Builder()
                .url(URL_TEST + URL_NOTIFICATION_TEST)
                .method(POST, body)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            return response.code()
        }

        fun transformNotification(noti: Noti) : NotificationAPI{
            return NotificationAPI(
                appName = noti.appName,
                appBundle = noti.appBundle,
                time = noti.createTime,
                title = noti.title,
                content = noti.content
            )
        }

        fun postNotificationWithFail(appName :String,context: Context){
            createNotificationChanel(context)

            var builder =
                NotificationCompat.Builder(context,CHANNEL_NOTIFICATION_SERVICE)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(appName)
                    .setContentText("$appName chưa gửi lên được Server!")
                    .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("$appName chưa gửi lên được Server!"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(113, builder.build())
            }

            //TODO: Ver 2 onClick gui lai
        }

        private fun createNotificationChanel(context: Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel =
                    NotificationChannel(CHANNEL_NOTIFICATION_SERVICE, NAME_CHANNEL, importance).apply {
                        description = DESCRIPTION_TEXT
                    }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}