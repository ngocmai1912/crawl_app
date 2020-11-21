package com.nmai.crawl.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TABLE_NOTIFICATION")
data class Noti(
    @PrimaryKey(autoGenerate = true) val _id : Int?,
    @ColumnInfo(name = "_appName") val appName : String,
    @ColumnInfo(name = "_appBundle") val appBundle : String,
    @ColumnInfo(name = "_createTime") val createTime : String,
    @ColumnInfo(name = "_title") val title : String,
    @ColumnInfo(name = "_content") val content : String,
    @ColumnInfo(name = "_checkPush") val checkPush : String
)