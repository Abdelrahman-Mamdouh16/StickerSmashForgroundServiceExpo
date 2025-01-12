package com.anonymous.StickerSmash
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.content.Intent
import android.os.Build
class MyServiceModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    init {
        reactContext = context
    }

    override fun getName(): String {
        return "MyServiceModule"
    }

    @ReactMethod
    fun startService() {
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
