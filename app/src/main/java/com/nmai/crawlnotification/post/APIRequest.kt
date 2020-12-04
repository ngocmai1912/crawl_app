package com.nmai.crawlnotification.post

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nmai.crawlnotification.MainActivity
import com.nmai.crawlnotification.R
import com.nmai.crawlnotification.repository.Noti
import okhttp3.*

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
                .url(URL_TEST + URL_NOTIFICATION_TEST)
                .method("GET", null)
                .build()

            val response: Response = client.newCall(request).execute()
            val json = response.body()?.string()
            //using TypeToken de tra ve
            return Gson().fromJson(json, object : TypeToken<List<NotificationAPI?>?>() {}.type)
        }

        /**
         * @param notificationAPI post notification
         * @return  True is post successfull
         * @return False is not post
         * */
        fun postNotification(notificationAPI: NotificationAPI): Int {
            val client = OkHttpClient().newBuilder()
                .build()
            val notificationToJson = Gson().toJson(notificationAPI)
            val body = RequestBody.create(mediaType, notificationToJson)
            val request: Request = Request.Builder()
                .url(URL + URL_NOTIFICATION)
                .method(POST, body)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            return response.code()
        }

        fun postNotificationWithFail(appName :String, time: String,context: Context){
            createNotificationChanel(context)

            val resultIntent = Intent(context, MainActivity::class.java)
            resultIntent.putExtra("post_again_notification",time)
            val resultPendingIntent : PendingIntent? =
                PendingIntent.getActivities(context,0, arrayOf(resultIntent),0)

            var builder =
                NotificationCompat.Builder(context,CHANNEL_NOTIFICATION_SERVICE)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.drawable.ic_noti)
                    .setContentTitle(appName)
                    .setContentText("$appName chưa được gửi lên Server!")
                    .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("$appName chưa được gửi lên Server!"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(113, builder.build())
            }


        }

        private fun createNotificationChanel(context: Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
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