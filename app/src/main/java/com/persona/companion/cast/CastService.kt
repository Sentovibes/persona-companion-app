package com.persona.companion.cast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.persona.companion.MainActivity
import com.persona.companion.R

/**
 * Foreground service to keep Cast server running in background
 */
class CastService : Service() {
    
    private val TAG = "CastService"
    private val CHANNEL_ID = "cast_service_channel"
    private val NOTIFICATION_ID = 1001
    private val binder = LocalBinder()
    
    private var castServer: CastServer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    inner class LocalBinder : Binder() {
        fun getService(): CastService = this@CastService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CastService created")
        createNotificationChannel()
        
        // Acquire wake lock to keep CPU running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PersonaCompanion::CastWakeLock"
        ).apply {
            acquire(10*60*60*1000L /*10 hours*/)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "CastService onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_CAST -> {
                try {
                    Log.d(TAG, "Starting foreground service...")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        // Android 14+ with special use type
                        startForeground(
                            NOTIFICATION_ID, 
                            createNotification("Starting..."), 
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, createNotification("Starting..."))
                    }
                    
                    Log.d(TAG, "Foreground service started, now starting cast server...")
                    startCastServer()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start foreground service", e)
                    e.printStackTrace()
                    stopSelf()
                }
            }
            ACTION_STOP_CAST -> {
                stopCastServer()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }
        
        return START_STICKY // Restart service if killed
    }
    
    private fun startCastServer() {
        // Not used anymore - server runs directly in CastManager
        Log.d(TAG, "startCastServer called but not needed")
    }
    
    private fun stopCastServer() {
        // Not used anymore
        Log.d(TAG, "stopCastServer called but not needed")
    }
    
    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "CastService destroyed")
        stopCastServer()
        
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cast Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps cast server running"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String = "Cast server running"): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Casting to TV")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    fun broadcastEnemy(enemy: Any) {
        castServer?.broadcastEnemy(enemy)
    }
    
    fun broadcastPersona(persona: Any) {
        castServer?.broadcastPersona(persona)
    }
    
    companion object {
        const val ACTION_START_CAST = "com.persona.companion.START_CAST"
        const val ACTION_STOP_CAST = "com.persona.companion.STOP_CAST"
        
        fun startService(context: Context) {
            val intent = Intent(context, CastService::class.java).apply {
                action = ACTION_START_CAST
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, CastService::class.java).apply {
                action = ACTION_STOP_CAST
            }
            context.startService(intent)
        }
    }
}
