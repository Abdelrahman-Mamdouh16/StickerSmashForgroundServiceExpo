package com.anonymous.StickerSmash

import android.content.Context
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
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import org.json.JSONArray
class MyForegroundService : Service() {

    private val CHANNEL_ID = "ForegroundServiceChannel"
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var socket: Socket
    private val userToken = ""
     private var isRunning = false
    override fun onCreate() {
        super.onCreate()
        connectToSocket()
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
        disconnectSocket()
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
            Log.d("Notification", "Created Notification Channel")
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
            // إرسال بيانات إلى الخادم بشكل دوري
            if (this::socket.isInitialized && socket.connected()) {
                val data = JSONObject()
                data.put("text", "Hello from Service!")
                socket.emit("message", data)
            }
            Toast.makeText(this, "Task is running...", Toast.LENGTH_SHORT).show()
            handler.postDelayed(runnable, 10000) // تكرار كل 10 ثوانٍ
        }
        handler.post(runnable)
    }
    private fun runOnUiThread(action: () -> Unit) {
        Handler(mainLooper).post(action)
    }

      private fun connectToSocket() {
        try {
            // استبدل هذا بـ عنوان خادمك الفعلي
            socket = IO.socket("")

            // الاتصال بالخادم
            socket.on(Socket.EVENT_CONNECT) {
                Log.d("Socket", "Connected")

                // إرسال الـ Token بعد الاتصال
                if (userToken.isNotEmpty()) {
                    socket.emit("auth", JSONObject().apply {
                        put("token", userToken)
                    })
                    Log.d("Socket", "Token sent to server: $userToken")
                }
            }

            socket.on("notifications") { args ->
                if (args.isNotEmpty()) {
                    val res = args[0] as JSONArray
                    for (i in 0 until res.length()) {
                        val item = res.getJSONObject(i)
                        // التعامل مع البيانات المستلمة (مثل الإشعارات)
                        Log.d("Notifications", item.toString())
                        val title = item.optString("title", "New Notification")
                        val body = item.optString("description", "You have a new message!")
                        displayNotification(title, body)
                    }
                }
            }

            socket.on("notificationAdded") { args ->
                if (args.isNotEmpty()) {
                    val res = args[0] as JSONObject
                    Log.d("Notification Added", res.toString())
                }
            }

            socket.on("orders") { args ->
                if (args.isNotEmpty()) {
                    val res = args[0] as JSONArray
                    for (i in 0 until res.length()) {
                        val item = res.getJSONObject(i)
                        // التعامل مع الأوامر مثل Clear Cache أو Clear Cookies
                        Log.d("Orders", item.toString())
                    }
                }
            }

            socket.on("orderAdded") { args ->
                if (args.isNotEmpty()) {
                    val res = args[0] as JSONObject
                    Log.d("Order Added", res.toString())
                }
            }

            socket.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
    private fun displayNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, notification)
    }

    private fun disconnectSocket() {
        if (this::socket.isInitialized) {
            socket.disconnect()
            socket.off()
        }
    }

}
