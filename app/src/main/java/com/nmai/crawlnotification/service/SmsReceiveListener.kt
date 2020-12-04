package com.nmai.crawlnotification.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import timber.log.Timber


class SmsReceiveListener : BroadcastReceiver() {

//    companion object{
//        var listener : SmsListener? = null
//        fun bindListener(l: SmsListener){
//            listener = l
//        }
//    }
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

                val PACKAGE_NAME_SMS = Settings.Secure.getString(
                    context?.contentResolver,
                    "sms_default_application"
                )
                val appName = "Tin nhắn"
                val appBundle = PACKAGE_NAME_SMS

//                listener?.let{
//                    it.messageReceived(
//                        "Tin nhắn",
//                        "sms",
//                        createTime,
//                        title,
//                        content
//                    )
//                }

                val bundle = Bundle()
                bundle.putString("app_name",appName)
                bundle.putString("app_bundle",appBundle)
                bundle.putString("create_time",createTime)
                bundle.putString("title",title)
                bundle.putString("content",content)

                if (context != null) {
                    SmsService.startService(context,bundle)
                }
            }
        }
    }

}