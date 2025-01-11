package com.anonymous.StickerSmash

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import android.content.pm.PackageManager
import androidx.core.content.PermissionChecker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Handler
import android.widget.Toast
import android.util.Log
class MyForegroundService : Service() {

    private val CHANNEL_ID = "ForegroundServiceChannel"
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Toast.makeText(this, "Channel is created...", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceWithNotification()
        startYourTask()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

   private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "My Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
}


    private fun startForegroundServiceWithNotification() {
         val fgCameraPermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_CAMERA)
        if (fgCameraPermission != PermissionChecker.PERMISSION_GRANTED) {
            
            // If CAMERA permission is not granted, stop the service
            Toast.makeText(this, "fgCameraPermission is false...", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Service is running...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()

            // Start the service as a foreground service with the appropriate type
            ServiceCompat.startForeground(
                this,
                100, // Cannot be 0
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                } else {
                    0
                }
            )
        } catch (e: Exception) {
            //if ( e is ForegroundServiceStartNotAllowedException) {
                // Handle the exception when the service is not allowed to start in foreground
                // (e.g., started from the background)

                Log.e("MyForegroundService", "Error starting foreground service", e)
                Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                //Toast.makeText(this, "Failed to start foreground service", Toast.LENGTH_SHORT).show()
            //}
        }
    }

    private fun startYourTask() {
        handler = Handler(mainLooper)
        runnable = Runnable {
            Toast.makeText(this, "Task is running...", Toast.LENGTH_SHORT).show()
            handler.postDelayed(runnable, 10000)
        }
        handler.post(runnable)
    }
}
