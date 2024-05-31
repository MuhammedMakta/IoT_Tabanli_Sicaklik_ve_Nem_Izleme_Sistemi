package com.example.isi_nem

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DataFetchReceiver : BroadcastReceiver() {

    private val channelId = "temperature_alert_channel"
    private val channelName = "Temperature Alerts"
    private var notificationSent = false

    override fun onReceive(context: Context, intent: Intent) {
        fetchData(context)
    }

    private fun fetchData(context: Context) {
        val url = URL("http://192.168.43.125/") // Replace with your URL
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.connect()
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))

            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            val result = response.toString()
            processResult(context, result)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
    }

    private fun processResult(context: Context, result: String) {
        // JSON verisini işle
        val jsonObject = JSONObject(result)
        val temperature = jsonObject.getDouble("temperature")

        // Sıcaklık 22 dereceyi geçtiğinde ve bildirim daha önce gönderilmediyse bildirim gönder
        if (temperature > 22 && !notificationSent) {
            sendNotification(context, "Sıcaklık Uyarısı", "Sıcaklık 22 dereceyi geçti: $temperature°C")
            notificationSent = true
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Bildirim kanalı oluşturma (yalnızca Android 8.0 ve üstü için gerekli)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirim oluşturma
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(androidx.loader.R.drawable.notification_bg) // Bildirim simgesini değiştirin
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Bildirimi gönderme
        notificationManager.notify(0, notification)
    }
}
