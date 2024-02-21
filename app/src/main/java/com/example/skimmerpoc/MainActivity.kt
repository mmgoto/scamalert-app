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
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
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
    private lateinit var message: List<String>;
    lateinit var sharedPreferences: SharedPreferences;
    var codeTyped: String? = null;

    private val smsReceiver = object : BroadcastSMS() {
        override fun onReceive(context: Context, intent: Intent) {
            val startTime = nanoTime()
            if (intent.action == "sms-received") {
                val sms = intent.getStringExtra("message");
                message = extractNumbersIfKeywordsPresent(sms.toString());
                try {
                    val codeInputFieldText = findViewById<EditText>(R.id.codeInputField);
                    val code = message[0];

                    if (code == "") {
                        Toast
                            .makeText(baseContext, "Digite um código a ser monitorado", Toast.LENGTH_LONG)
                            .show();
                    } else {
                        Toast
                            .makeText(baseContext, "Código monitorado: $code", Toast.LENGTH_LONG)
                            .show();
                        sharedPreferences.edit().putString(
                            "codeTyped", code
                        ).apply();
                        val endTime = nanoTime()
                        val duration = endTime - startTime
                        Log.d("MyApp", "Tempo de execução: $duration nanosegundos")
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
        sharedPreferences = this.getSharedPreferences(
            "com.example.skimmerpoc",
            Context.MODE_PRIVATE
        );

        Keylogger.requestPermission(this)
        createNotificationChannel();
        permissionSMS();
        lifecycleScope.launch {
            Keylogger.subscribe { entry ->
                var value = entry.toString();
                codeTyped = sharedPreferences.getString("codeTyped", "").toString();
                if(codeTyped!!.isNotEmpty() && value.contains(codeTyped.toString())){
                    Log.e("TYPED THE RIGHT CODE", value);
                    var notification = NotificationCompat.Builder(this@MainActivity, "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Cuidado, não compartilhe!")
                        .setContentText("Você acabou de digitar um código sensível e que não deve ser compartilhado!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build()
                    with(NotificationManagerCompat.from(this@MainActivity)){
                        notify(Random(3321).nextInt(), notification)
                    }
                }
                Log.d("KEYLOGGER", entry.toString())
            }
        }

        setContentView(R.layout.activity_main)
    }

    @SuppressLint("WrongConstant")
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My notification channel"
            val descriptionText = "My notification channel description"
            val importance = NotificationManager.IMPORTANCE_MAX;
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun permissionSMS() {
        //Verificação se a permissão já foi aceita ou não
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED){
        //Se caso a permissão ainda não foi aceita, monta a caixa para pedir permissão
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS),10);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
            PackageManager.PERMISSION_GRANTED){
        //Se caso a permissão ainda não foi aceita, monta a caixa para pedir permissão
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS),11);
        }
    }

    fun onSubmit(view: View){
        try {
            val codeInputFieldText = findViewById<EditText>(R.id.codeInputField);
            //incluir na variavel code o codigo detectado (via SMS)
            val code = codeInputFieldText.text.toString();

            if (code == "") {
                Toast
                    .makeText(this, "Digite um código a ser monitorado", Toast.LENGTH_LONG)
                    .show();
            } else {
                Toast
                    .makeText(this, "Código monitorado: $code", Toast.LENGTH_LONG)
                    .show();
                sharedPreferences.edit().putString(
                    "codeTyped", code
                ).apply();
            }

            Log.e("botão", "clicou");
            val content = codeInputFieldText.text.toString();
            Log.e("Content retrieved", content);
        } catch (e : Exception) {
            Log.e("Content retrieved", e.toString());
        }
    }

//    override fun onSmsReceived(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//        println(message)
//    }


//    override fun onDestroy() {
//        super.onDestroy()
//        // Unregister the SmsReceiver when the activity is destroyed
//        unregisterReceiver(smsReceiver)
//    }

}