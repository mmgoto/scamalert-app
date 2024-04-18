package com.example.skimmerpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
open class BroadcastSMS : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        val pdus = extras!!["pdus"] as Array<*>?
        val sms: Array<SmsMessage?> = arrayOfNulls<SmsMessage>(pdus!!.size)
        var conteudoSMS = ""
        for (i in sms.indices) {
            sms[i] = SmsMessage.createFromPdu(
                pdus!![i] as ByteArray,
                extras!!.getString("format")
            )
            conteudoSMS += sms[i]?.getMessageBody()
        }
        if(conteudoSMS != null){
            val localIntent = Intent("sms-received")
            localIntent.putExtra("message", conteudoSMS)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }
}