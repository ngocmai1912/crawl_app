package com.nmai.crawlnotification.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage


class SmsReceiveListener : BroadcastReceiver() {

    companion object{
        var listener : SmsListener? = null
        fun bindListener(l: SmsListener){
            listener = l
        }
    }
    // lay data tu tin nhan moi nhan
    override fun onReceive(context: Context?, intent: Intent) {
        val data = intent.extras
        val pdus = data!!["pdus"] as Array<*>?
        for (i in pdus!!.indices) {
            val smsMessage: SmsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray)
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