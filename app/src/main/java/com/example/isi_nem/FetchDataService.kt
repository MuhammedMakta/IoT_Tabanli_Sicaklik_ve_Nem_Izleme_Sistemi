package com.example.isi_nem

import android.annotation.SuppressLint


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FetchDataService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 60000 // 60 saniye
    private val channelId = "temperature_alert_channel"
    private val channelName = "Temperature Alerts"
    private var notificationSent = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(fetchDataRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(fetchDataRunnable)
    }

    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchData()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            val result = fetchTemperatureData("http://192.168.43.125/")
            withContext(Dispatchers.Main) {
                processResult(result)
            }
        }
    }

    private suspend fun fetchTemperatureData(url: String): String {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        return try {
            urlConnection.connect()
            val inputStream = urlConnection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            response.toString()
        } finally {
            urlConnection.disconnect()
        }
    }

    private fun processResult(result: String) {
        val jsonObject = JSONObject(result)
        val temperature = jsonObject.getDouble("temperature")
        val humidity = jsonObject.getDouble("humidity")

        // Sıcaklık 22 dereceyi geçtiğinde ve bildirim daha önce gönderilmediyse bildirim gönder
        if (temperature > 22 && !notificationSent) {
            sendNotification("Sıcaklık Uyarısı", "Sıcaklık 22 dereceyi geçti: $temperature°C")
            notificationSent = true
        }
        // Sıcaklık 22 dereceyi geçtiğinde ve bildirim daha önce gönderilmediyse bildirim gönder
        if (temperature < 20 && !notificationSent) {
            sendNotification("Sıcaklık Uyarısı", "Sıcaklık 20 derecenin altına düştü: $temperature°C")
            notificationSent = true
        }
        // Sıcaklık 22 dereceyi geçtiğinde ve bildirim daha önce gönderilmediyse bildirim gönder
        if (humidity > 65 && !notificationSent) {
            sendNotification("Nem Uyarısı", "Nem %65'i geçti: $humidity%")
            notificationSent = true
        }
        // Sıcaklık 22 dereceyi geçtiğinde ve bildirim daha önce gönderilmediyse bildirim gönder
        if (humidity < 20 && !notificationSent) {
            sendNotification("Nem Uyarısı", "Nem %65'in altına düştü: $humidity%")
            notificationSent = true
        }

        // Verileri bir yayma içinde ana aktiviteye gönder
        val intent = Intent("com.example.TEMPERATURE_UPDATE")
        intent.putExtra("temperature", temperature)
        intent.putExtra("humidity", humidity)
        sendBroadcast(intent)
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Bildirim oluşturma
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(androidx.coordinatorlayout.R.drawable.notification_bg) // Bildirim simgesini değiştirin
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Bildirimi gönderme
        notificationManager.notify(0, notification)
    }

    private fun createNotificationChannel() {
        // Android 8.0 (Oreo) ve üstü için bildirim kanalı oluşturma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundServiceWithNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Veri Servisi Çalışıyor")
            .setContentText("Sıcaklık ve nem verileri izleniyor.")
            .setSmallIcon(androidx.core.R.drawable.notification_bg) // Bildirim simgesini değiştirin
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
