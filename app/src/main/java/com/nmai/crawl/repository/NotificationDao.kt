package com.nmai.crawl.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotificationDao {
    @Query("SELECT * FROM noti")
    fun getAll() : List<Noti>
    @Insert
    fun insert(notificationData: Noti)
    @Update
    fun update(notificationData: Noti)
}