package com.nmai.crawlnotification.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import timber.log.Timber


class SmsReceiveListener : BroadcastReceiver() {

    // lay data tu tin nhan moi nhan
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action.equals("android.provider.Telephony.SMS_RECEIVED")){

            val PACKAGE_NAME_SMS = Settings.Secure.getString(
                context?.contentResolver,
                "sms_default_application"
            )

            val data = intent.extras
            val pdus = data!!["pdus"] as Array<*>?
            for (i in pdus!!.indices) {

                var smsMessage: SmsMessage = SmsMessage.createFromPdu(
                    pdus[i] as ByteArray?, data.getString("format")
                )
                val title =  smsMessage.displayOriginatingAddress.toString()
                val createTime = smsMessage.timestampMillis.toString()
                val content = smsMessage.messageBody.toString()


                val appName = Telephony.Sms.getDefaultSmsPackage(context)
                var appBundle = "unknown"
                if(PACKAGE_NAME_SMS != null ){
                    appBundle = PACKAGE_NAME_SMS
                }
                val pm = context?.packageManager
                val ai: ApplicationInfo? = pm?.getApplicationInfo(appName, 0)
                val applicationName =
                    (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String
                Timber.d("test package $applicationName")

                val bundle = Bundle()
                bundle.putString("app_name",applicationName)
                bundle.putString("app_bundle", appName)
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