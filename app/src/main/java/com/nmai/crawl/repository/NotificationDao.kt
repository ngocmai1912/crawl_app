package com.nmai.crawl.repository

import androidx.room.*

@Dao
interface NotificationDao {
    @Query("SELECT * FROM TABLE_NOTIFICATION")
    fun getAll() : List<Noti>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(notificationData: Noti)

    @Update
    fun update(vararg notificationData: Noti)

    @Query("SELECT * FROM TABLE_NOTIFICATION WHERE _content = :content")
    fun getNotificationWithContent(content: String) : Noti
}