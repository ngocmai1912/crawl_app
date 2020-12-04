package com.nmai.crawlnotification.repository

import androidx.room.*

@Dao
interface NotificationDao {
    @Query("SELECT * FROM TABLE_NOTIFICATION")
    fun getAll() : List<Noti>

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _checkPush LIKE :checkPush")
    fun getNotificationFailPost(checkPush: String) : List<Noti>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg notificationData: Noti)

    @Update
    fun update(vararg notificationData: Noti)

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _createTime = :time")
    fun getNotificationWithTime(time: String) : Noti

    @Query("SELECT COUNT(_checkPush) FROM TABLE_NOTIFICATION WHERE _checkPush = 'false' ")
    fun getCount() : Int
}