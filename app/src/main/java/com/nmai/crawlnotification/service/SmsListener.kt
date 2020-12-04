package com.nmai.crawlnotification.service

interface SmsListener {
    fun messageReceived(
        appName : String,
        appBundle : String,
        createTime : String,
        title : String,
        content : String
    )
}