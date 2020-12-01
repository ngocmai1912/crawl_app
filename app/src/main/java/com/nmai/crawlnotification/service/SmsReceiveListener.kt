package com.nmai.crawlnotification.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import com.nmai.crawlnotification.repository.NotificationDatabase
import timber.log.Timber


class SmsReceiveListener : BroadcastReceiver() {


    // lay data tu tin nhan moi nhan
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action.equals("android.provider.Telephony.SMS_RECEIVED")){
            val data = intent.extras
            val pdus = data!!["pdus"] as Array<*>?
            for (i in pdus!!.indices) {

                var smsMessage: SmsMessage = SmsMessage.createFromPdu(
                    pdus[i] as ByteArray?, data.getString("format")
                )
                val title =  smsMessage.displayOriginatingAddress.toString()
                val createTime = smsMessage.timestampMillis.toString()
                val content = smsMessage.messageBody.toString()
                val appName = "Tin nhắn"
                val appBundle = "sms"

                if (context != null) {
                    SmsService.startService(context,appName,appBundle,createTime,title,content)
                }
            }
        }
    }

}