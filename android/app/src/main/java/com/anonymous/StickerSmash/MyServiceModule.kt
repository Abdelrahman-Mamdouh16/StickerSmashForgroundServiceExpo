package com.anonymous.StickerSmash
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.content.Intent

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
        reactContext.startForegroundService(serviceIntent)
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
