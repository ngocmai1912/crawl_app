package com.nmai.crawl.model

import com.nmai.crawl.MainActivity
import com.nmai.crawl.repository.Noti
import com.nmai.crawl.repository.NotificationDatabase

object Data {
    var list : List<Noti> = NotificationDatabase.getInstance(MainActivity.context!!).notificationDao().getAll()
    fun getAll() : List<com.nmai.crawl.model.NotificationData>{

        var listData = mutableListOf<com.nmai.crawl.model.NotificationData>()
        for(i in list){
            var check : Boolean = false
            if(i.checkPush == "true") check = true
            listData.add(
                com.nmai.crawl.model.NotificationData(
                    i.appName, i.appBundle, i.createTime, i.title, i.content, check
                )
            )
        }
        return listData
    }
//    fun insert(notificationData: com.nmai.crawl.model.NotificationData){
//        var check = "false"
//        if(notificationData.checkPush) check = "true"
//        NotificationDatabase.getInstance(MainActivity.context!!).notificationDao().insert(
//            Noti(
//                System.currentTimeMillis().toString(),
//                notificationData.appName,
//                notificationData.appBundle,
//                notificationData.createTime,
//                notificationData.title,
//                notificationData.content,
//                check
//            )
//        )
//    }
    fun update(notificationData: NotificationData){
        var check = "false"
        if(notificationData.checkPush) check = "true"
        NotificationDatabase.getInstance(MainActivity.context!!).notificationDao().insert(
            Noti(
                System.currentTimeMillis().toInt(),
                notificationData.appName,
                notificationData.appBundle,
                notificationData.createTime,
                notificationData.title,
                notificationData.content,
                check
            )
        )
    }
}