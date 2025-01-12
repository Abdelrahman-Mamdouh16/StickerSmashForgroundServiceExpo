package com.anonymous.StickerSmash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, MyForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             // للأجهزة التي تعمل بالإصدار 26 أو أعلى
            context.startForegroundService(serviceIntent)
            } else {
             // للأجهزة التي تعمل بإصدارات أقدم
            context.startService(serviceIntent)
            }
        }
    }
}
