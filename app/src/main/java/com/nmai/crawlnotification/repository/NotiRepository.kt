package com.nmai.crawlnotification.repository

import androidx.lifecycle.LiveData

class NotiRepository(val dao : NotificationDao) {
    fun getAll(): LiveData<MutableList<Noti>> = dao.getAll()
    fun insert(noti: Noti) = dao.insert(noti)
    fun update(noti: Noti) = dao.update(noti)
    fun getNotiWithTime(time: String) = dao.getNotificationWithTime(time)
}