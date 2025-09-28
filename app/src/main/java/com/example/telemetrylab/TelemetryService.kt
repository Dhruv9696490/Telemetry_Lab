package com.example.telemetrylab

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.system.measureTimeMillis


class TelemetryService: Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val frameChannel = Channel<Long>(capacity = 2)
    private var producerJob: Job? = null
    private var consumerJob: Job? = null
    private var computeLoad = 2
    private var running = false

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int):
            Int {
        createNotificationChannel()
        startForeground(1, createNotification())
        computeLoad = intent?.getIntExtra("computeLoad", 2) ?: 2
        running = true
        startProducerConsumerLoop()
        return START_STICKY
    }
    override fun onDestroy() {
        running = false
        scope.cancel()
        super.onDestroy()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager
            val ch = NotificationChannel("telemetry", "Telemetry",
                NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }
    private fun createNotification(): Notification =NotificationCompat.Builder(this, "telemetry")
        .setContentTitle("Telemetry Lab")
        .setContentText("Running compute loop")
        .setSmallIcon(android.R.drawable.stat_notify_more)
        .build()
    private fun startProducerConsumerLoop() {
        producerJob?.cancel()
        consumerJob?.cancel()
        producerJob = scope.launch {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val powerSave = if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.LOLLIPOP) pm.isPowerSaveMode else false
            val frequency = if (powerSave) 10 else 20
            val periodMs = 1000L / frequency
            while (isActive) {
                frameChannel.trySend(System.nanoTime())
                delay(periodMs)
            }
        }
        consumerJob = scope.launch {
            for (token in frameChannel) {
                val t = measureTimeMillis {
                    Compute.runComputePass(computeLoad)
                }
                AppStatus.postLatency(t.toDouble())
            }
        }
    }
}