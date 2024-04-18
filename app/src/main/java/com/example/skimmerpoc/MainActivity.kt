package com.example.skimmerpoc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.evilthreads.keylogger.Keylogger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import java.lang.System.nanoTime
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var newTokenSmsList: List<String>;
    val tokenList = mutableListOf<String>();
    private val smsReceiver = object : BroadcastSMS() {
        override fun onReceive(context: Context, intent: Intent) {
            //val startTime = nanoTime()
            if (intent.action == "sms-received") {
                newTokenSmsList = extractNumbersIfKeywordsPresent(intent.getStringExtra("message").toString());
                try {
                    val codeInputFieldText = findViewById<EditText>(R.id.codeInputField);
                    Log.d("TesteTokenList", "Lista recebida: $newTokenSmsList")
                    if (newTokenSmsList.isEmpty()) {
                        Toast
                            .makeText(baseContext, "Digite um código a ser monitorado", Toast.LENGTH_LONG)
                            .show();
                    } else {
                        addToken(newTokenSmsList);
                        Toast
                            .makeText(baseContext, "Código monitorado: $tokenList", Toast.LENGTH_LONG)
                            .show();
                        //val endTime = nanoTime()
                        //val duration = endTime - startTime
                        //Log.d("MyApp", "Tempo de execução: $duration nanosegundos")
                    }

                    Log.e("botão", "clicou");
                    val content = codeInputFieldText.text.toString();
                    Log.e("Content retrieved", content);
                } catch (e : Exception) {
                    Log.e("Content retrieved", e.toString());
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        val filter = IntentFilter("sms-received")
        LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, filter)
        Keylogger.requestPermission(this)
        createNotificationChannel();
        permissionSMS();
        lifecycleScope.launch {
            Keylogger.subscribe { entry ->
                var tokenKeyloggerList = extractNumbersIfKeywordsPresent("token" + entry.text)
                if(tokenList.isNotEmpty() && containsAny(tokenKeyloggerList, tokenList)){
                    //val startTime = nanoTime()
                    Log.e("TYPED THE RIGHT CODE", entry.toString());
                    var notification = NotificationCompat.Builder(this@MainActivity, "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("ISSO PODE SER UM GOLPE!")
                        .setContentText("Você digitou um código sensível, este deve ser utilizando apenas no local destinado a isso, na aplicação de origem!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build()
                    with(NotificationManagerCompat.from(this@MainActivity)){
                        notify(Random(3321).nextInt(), notification)
                    }
                    //val endTime = nanoTime()
                    //val duration = endTime - startTime
                    //Log.d("MyApp", "Tempo de execução: $duration nanosegundos")
                }
                Log.d("KEYLOGGER", entry.toString())
            }
        }

        setContentView(R.layout.activity_main)
    }

    @SuppressLint("WrongConstant")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My notification channel"
            val descriptionText = "My notification channel description"
            val importance = NotificationManager.IMPORTANCE_MAX;
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun permissionSMS() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS),10);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS),11);
        }
    }

    fun onSubmit(view: View){
        try {
            val codeInputFieldText = findViewById<EditText>(R.id.codeInputField);
            val code = codeInputFieldText.text.toString();
            if (code == "") {
                Toast
                    .makeText(this, "Digite um código a ser monitorado", Toast.LENGTH_LONG)
                    .show();
            } else {
                addToken(code.split(" "))
                Toast
                    .makeText(this, "Códigos monitorados: $tokenList", Toast.LENGTH_LONG)
                    .show();
            }
            Log.e("botão", "clicou");
            val content = codeInputFieldText.text.toString();
            Log.e("Content retrieved", content);
        } catch (e : Exception) {
            Log.e("Content retrieved", e.toString());
        }
    }

    fun addToken(token: List<String>){
        tokenList.addAll(0, token);
        if(tokenList.size > 10) {
            for (i in 1..(tokenList.size-10)){
                tokenList.removeLast();
            }
        }
    }

    fun containsAny(list1: List<String>, list2: List<String>): Boolean {
        return list1.any { list2.contains(it) }
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