package com.nmai.crawl.model

data class NotificationData (
    val appName : String,
    val appBundle : String,
    val createTime : String,
    val title : String,
    val content : String,
    val checkPush : Boolean
)