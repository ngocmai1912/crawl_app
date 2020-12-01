package com.nmai.crawlnotification.repository

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {
    @Query("SELECT * FROM TABLE_NOTIFICATION")
    fun getAll() : LiveData<MutableList<Noti>>

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _checkPush LIKE :checkPush")
    fun getNotificationFailPost(checkPush: String) : LiveData<MutableList<Noti>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg notificationData: Noti)

    @Update
    fun update(vararg notificationData: Noti)

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _createTime = :time")
    fun getNotificationWithTime(time: String) : Noti
}