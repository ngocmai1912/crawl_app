package com.nmai.crawl.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Noti::class], version = 1)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao() : NotificationDao
    companion object{
        @Volatile var instance : NotificationDatabase? = null
        fun getInstance(context: Context) : NotificationDatabase {
            if(instance == null) instance =
                buidDatabase(context)
            return instance!!
        }

        fun buidDatabase(context: Context) : NotificationDatabase {
            return Room.databaseBuilder(context, NotificationDatabase::class.java, "notificationData.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}