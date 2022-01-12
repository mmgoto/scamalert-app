package com.example.skimmerpoc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.evilthreads.keylogger.Keylogger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences;
    var codeTyped: String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences(
            "com.example.skimmerpoc",
            Context.MODE_PRIVATE
        );

        Keylogger.requestPermission(this)
        createNotificationChannel();

        lifecycleScope.launch {
            Keylogger.subscribe { entry ->
                var value = entry.toString();
                codeTyped = sharedPreferences.getString("codeTyped", "");
                if(value.contains(codeTyped.toString())){
                    Log.e("TYPED THE RIGHT CODE", value);
                    var notification = NotificationCompat.Builder(this@MainActivity, "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Be careful!")
                        .setContentText("You just typed a sensitive code")
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My notification channel"
            val descriptionText = "My notification channel description"
            val importance = NotificationManager.IMPORTANCE_HIGH;
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
}