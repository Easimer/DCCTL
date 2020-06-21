package net.easimer.dcctl

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.easimer.dcctl.camera.CameraService

object CameraServiceManager {
    private val TAG = "CameraServiceManager"
    private var serviceIntent: Intent? = null

    @Synchronized
    fun startIfDoesntExist(ctx: Context): Boolean {
        if(serviceIntent == null) {
            Log.d(TAG, "Creating the camera service")
            Intent(ctx, CameraService::class.java).also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(it)
                } else {
                    ctx.startService(it)
                }
                serviceIntent = it
            }
            return true
        }
        return false
    }

    @Synchronized
    fun stopIfRunning(ctx: Context): Boolean {
        if(serviceIntent != null) {
            Log.d(TAG, "Stopping the camera service")
            ctx.stopService(serviceIntent)
            serviceIntent = null
            return true
        }
        return false
    }
}