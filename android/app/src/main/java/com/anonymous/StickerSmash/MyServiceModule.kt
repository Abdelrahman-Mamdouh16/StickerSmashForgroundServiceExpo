package com.anonymous.StickerSmash
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.content.Intent
import android.os.Build
import android.util.Log
class MyServiceModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    init {
        reactContext = context
    }

    override fun getName(): String {
        return "MyServiceModule"
    }

    private var isRunning = false // متغير لتتبع حالة الخدمة

@ReactMethod
fun startService() {
    if (isRunning) {
        // إذا كانت الخدمة تعمل بالفعل، لا تقم بتشغيلها مرة أخرى
        Log.d("MyForegroundService", "Service is already running.")
        return
    }

    // قم بتعيين الحالة إلى قيد التشغيل
    isRunning = true

    val serviceIntent = Intent(reactContext, MyForegroundService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // للأجهزة التي تعمل بالإصدار 26 أو أعلى
        reactContext.startForegroundService(serviceIntent)
    } else {
        // للأجهزة التي تعمل بإصدارات أقدم
        reactContext.startService(serviceIntent)
    }
}

    @ReactMethod
    fun stopService() {
        val serviceIntent = Intent(reactContext, MyForegroundService::class.java)
        reactContext.stopService(serviceIntent)
    }

    companion object {
        private lateinit var reactContext: ReactApplicationContext
    }
}
