package com.example.skimmerpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import androidx.localbroadcastmanager.content.LocalBroadcastManager

open class BroadcastSMS : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //Recuperar os extras que estão dentro da intent
        //A classe Bundle recupera todos os extras, independente do tipo
        val extras = intent.extras

        //Extrair somente o protocolo PDU (Protocol Data Unit) dos extras
        //O PDU é utilizado para envio de mensagens SMS
        val pdus = extras!!["pdus"] as Array<*>?

        //Criação de um vetor da classe SmsMessage à partir dos pdus (SMS) recebidos
        val sms: Array<SmsMessage?> = arrayOfNulls<SmsMessage>(pdus!!.size)

        var conteudoSMS = ""

        //Laço para percorrer todos os SMSs que chegaram na mensagem
        for (i in sms.indices) {
            sms[i] = SmsMessage.createFromPdu(
                pdus!![i] as ByteArray,
                extras!!.getString("format")
            )

            //Concatenar o conteúdo da mensagem SMS
            conteudoSMS += sms[i]?.getMessageBody()
        }

        if(conteudoSMS != null){
            val localIntent = Intent("sms-received")
            localIntent.putExtra("message", conteudoSMS)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }


    fun extractNumbersIfKeywordsPresent(input: String): List<String> {
        val wordsToCheck = listOf("token", "verificacao", "codigo", "senha", "segredo", "password")
        val normalizedInput = input.normalize()
        val containsKeyword = wordsToCheck.any { normalizedInput.contains(it, ignoreCase = true) }
        return if (containsKeyword) {
            Regex("""\d+""").findAll(input).map { it.value }.toList()
        } else {
            emptyList()
        }
    }


    private fun String.normalize(): String {
        val original = arrayOf('á', 'à', 'ã', 'â', 'é', 'ê', 'í', 'ó', 'ô', 'õ', 'ú', 'ü', 'ç')
        val normalized = arrayOf('a', 'a', 'a', 'a', 'e', 'e', 'i', 'o', 'o', 'o', 'u', 'u', 'c')
        return this.map { char ->
            val index = original.indexOf(char)
            if (index >= 0) normalized[index] else char
        }.joinToString("")
    }

}


//https://douglasgaspar.wordpress.com/2020/03/20/utilizacao-do-broadcastreceiver-com-android/