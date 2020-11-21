package com.nmai.crawlnotification.repository

import androidx.room.*

@Dao
interface NotificationDao {
    @Query("SELECT * FROM TABLE_NOTIFICATION")
    fun getAll() : List<Noti>

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _checkPush LIKE :checkPush")
    fun getNotificationFailPost(checkPush: String) : List<Noti>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(notificationData: Noti)

    @Update
    fun update(vararg notificationData: Noti)

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _content = :content")
    fun getNotificationWithContent(content: String) : Noti
}