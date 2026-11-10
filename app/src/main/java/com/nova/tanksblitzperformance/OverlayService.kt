package com.nova.tanksblitzperformance

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import java.util.Locale

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: TextView? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateTask = object : Runnable {
        override fun run() {
            updateOverlay()
            handler.postDelayed(this, 2000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (android.provider.Settings.canDrawOverlays(this)) {
            showOverlay()
            handler.post(updateTask)
        } else {
            stopSelf()
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val view = TextView(this).apply {
            text = "TB PERFORMANCE\nЧтение датчиков…"
            textSize = 12f
            setTextColor(0xFFF2F4F0.toInt())
            setBackgroundColor(0xE619201D.toInt())
            setPadding(18, 14, 18, 14)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 12
            y = 120
        }

        windowManager.addView(view, params)
        overlayView = view
    }

    private fun updateOverlay() {
        val batteryTemp = readBatteryTemperature()
        val memory = readMemory()
        val thermal = readThermalStatus()

        val tempText = batteryTemp?.let {
            String.format(Locale.US, "%.1f°C", it)
        } ?: "N/A"

        overlayView?.text = buildString {
            append("TB PERFORMANCE  •  MAX\n")
            append("FPS: N/A\n")
            append("АКБ: $tempText\n")
            append("RAM: ${memory.first} / ${memory.second} MB\n")
            append("THERMAL: $thermal")
        }
    }

    private fun readBatteryTemperature(): Float? {
        val intent = registerReceiver(
            null,
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return null
        val raw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        return if (raw >= 0) raw / 10f else null
    }

    private fun readMemory(): Pair<Long, Long> {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val info = android.app.ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        val mb = 1024L * 1024L
        val total = info.totalMem / mb
        val available = info.availMem / mb
        return Pair(total - available, total)
    }

    private fun readThermalStatus(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "unsupported"
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return when (powerManager.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> "NORMAL"
            PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
            PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
            PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
            PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
            else -> "UNKNOWN"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TB Performance monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("TB Performance")
            .setContentText("Плавающий монитор активен")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateTask)
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "tb_performance_monitor"
        private const val NOTIFICATION_ID = 1201
    }
}
