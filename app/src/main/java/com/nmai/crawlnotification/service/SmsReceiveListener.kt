package com.nmai.crawlnotification.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import timber.log.Timber


class SmsReceiveListener : BroadcastReceiver() , SmsListener{

    companion object{
        var listener : SmsListener? = null
        fun bindListener(l: SmsListener){
            listener = l
        }
    }
    // lay data tu tin nhan moi nhan
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
                listener?.let{
                    it.messageReceived(
                        "Tin nhắn",
                        "sms",
                        createTime,
                        title,
                        content
                    )
                }
            }

        }
    }

    override fun messageReceived(
        appName: String,
        appBundle: String,
        createTime: String,
        title: String,
        content: String
    ) {
        TODO("Not yet implemented")
    }

}