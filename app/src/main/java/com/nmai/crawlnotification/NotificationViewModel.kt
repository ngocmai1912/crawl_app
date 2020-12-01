package com.nmai.crawlnotification

import android.content.Context
import androidx.lifecycle.*
import com.nmai.crawlnotification.model.NotificationData
import com.nmai.crawlnotification.post.APIRequest
import com.nmai.crawlnotification.post.NotificationAPI
import com.nmai.crawlnotification.repository.Noti
import com.nmai.crawlnotification.repository.NotiRepository
import com.nmai.crawlnotification.repository.NotificationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationViewModel(context: Context): ViewModel() {
    private val notiRepository : NotiRepository
    val listNotification : LiveData<MutableList<NotificationData>>

    init {
        val dao = NotificationDatabase.getInstance(context).notificationDao()
        notiRepository = NotiRepository(dao)

        listNotification = Transformations.map(notiRepository.getAll()){
            it.map { noti ->
                var isTrue = true
                if(noti.checkPush == "true") isTrue = true
                else if(noti.checkPush == "false") isTrue = false
                NotificationData(
                    noti.appName,
                    noti.appBundle,
                    noti.createTime,
                    noti.title,
                    noti.content,
                    isTrue
                )
            } as MutableList<NotificationData>
        }
    }

    fun addItemToListNotification(notificationData: NotificationData){
        listNotification.value?.add(notificationData)
        var status = "true"
        status = if (notificationData.checkPush) "true"
        else "false"
        val noti = Noti(
            null,
            notificationData.appName,
            notificationData.appBundle,
            notificationData.createTime,
            notificationData.title,
            notificationData.content,
            status
        )
        viewModelScope.launch(Dispatchers.IO){
            notiRepository.insert(noti)
        }
    }
    fun postNotificationToServer(time: String){
        viewModelScope.launch(Dispatchers.IO){
            val notificationDB = notiRepository.getNotiWithTime(time)

            val appName = notificationDB.appName
            val bundle = notificationDB.appBundle
            val time = notificationDB.createTime
            val title = notificationDB.title
            val content = notificationDB.content

            val notificationPost : NotificationAPI = NotificationAPI(appName,bundle,time,title,content)

            try {
                val isSuccess =  APIRequest.postNotification(notificationPost)
                if (isSuccess == 200) {
                    Timber.d("post notification Success!!")
                    notificationDB.checkPush = "true"

                    notiRepository.update(notificationDB)
                }
            }
            catch (e: Exception){
                Timber.d("post fail!")
            }
        }
    }
}
