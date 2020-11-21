package com.nmai.crawlnotification.post

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nmai.crawlnotification.repository.Noti
import okhttp3.*
import timber.log.Timber

class APIRequest {
    companion object {
        private const val URL = "https://bepay2.com"
        private const val URL_NOTIFICATION = "/api/v2/notification/send"
        private const val POST = "POST"
        private const val GET = "GET"
        private val mediaType: MediaType? = MediaType.parse("application/json")

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
                .url(URL + URL_NOTIFICATION)
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
    }
}