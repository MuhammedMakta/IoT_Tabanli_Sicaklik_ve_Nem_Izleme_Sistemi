package com.example.isi_nem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewHumidity: TextView

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val temperature = intent?.getDoubleExtra("temperature", -1.0)
            val humidity = intent?.getDoubleExtra("humidity", -1.0)

            if (temperature != null && humidity != null) {
                textViewTemperature.text = "Sıcaklık: $temperature°C"
                textViewHumidity.text = "Nem: $humidity%"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewTemperature = findViewById(R.id.textViewTemperature)
        textViewHumidity = findViewById(R.id.textViewHumidity)

        // Servisi başlat
        val serviceIntent = Intent(this, FetchDataService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Yayma alıcısını kaydet
        val filter = IntentFilter("com.example.TEMPERATURE_UPDATE")
        registerReceiver(dataReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Yayma alıcısını kaldır
        unregisterReceiver(dataReceiver)
    }
}



