package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackingService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        private const val CHANNEL_ID = "smartmeta_tracking_channel"
        private const val NOTIFICATION_ID = 1001

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

        private val _simulatedLocation = MutableStateFlow(Pair(-6.2088, 106.8456)) // Jakarta coords default
        val simulatedLocation: StateFlow<Pair<Double, Double>> = _simulatedLocation

        fun startService(context: Context) {
            val intent = Intent(context, TrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrackingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _isServiceRunning.value = true
        
        // Start as foreground service
        val notification = createNotification("Pelacakan Lokasi Petugas Aktif", "Aplikasi SMARTMETA sedang melacak posisi Anda di latar belakang.")
        startForeground(NOTIFICATION_ID, notification)

        // Simulate position changes every 5 seconds to represent real-time movement
        scope.launch {
            var lat = -6.2088
            var lng = 106.8456
            while (_isServiceRunning.value) {
                delay(5000)
                // Random walk around Jakarta
                lat += (Math.random() - 0.5) * 0.001
                lng += (Math.random() - 0.5) * 0.001
                _simulatedLocation.value = Pair(lat, lng)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(title: String, text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "SMARTMETA Tracking Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Saluran notifikasi pelacakan lokasi latar belakang SMARTMETA"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
