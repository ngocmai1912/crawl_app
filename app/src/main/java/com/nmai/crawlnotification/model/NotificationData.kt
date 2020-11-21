package com.nmai.crawlnotification.model

data class NotificationData (
    val appName : String,
    val appBundle : String,
    val createTime : String,
    val title : String,
    val content : String,
    var checkPush : Boolean
)