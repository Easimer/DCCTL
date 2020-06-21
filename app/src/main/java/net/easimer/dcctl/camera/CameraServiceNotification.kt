package net.easimer.dcctl.camera

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import net.easimer.dcctl.MAIN_ACTIVITY_EXTRA_REQUEST
import net.easimer.dcctl.MAIN_ACTIVITY_REQUEST_STOP_CAMERA_SERVICE
import net.easimer.dcctl.MainActivity
import net.easimer.dcctl.R

class CameraServiceNotification(private val ctx: Service) {
    private val notifyMan =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val stopIntent = Intent(ctx, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .let {
            it.putExtra(MAIN_ACTIVITY_EXTRA_REQUEST, MAIN_ACTIVITY_REQUEST_STOP_CAMERA_SERVICE)
            PendingIntent.getActivity(
                ctx,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    private val actTerminate =
        NotificationCompat.Action.Builder(R.drawable.stop, ctx.getText(R.string.terminate_service), stopIntent)
            .build()

    private val notificationBuilderTemplate = makeNotificationBuilder()
        .setContentTitle(ctx.getText(R.string.notification_camera_is_active))
        .setSmallIcon(R.drawable.notification)
        .addAction(actTerminate)
        .setOngoing(true)

    fun create() {
        val notifyFmt = ctx.getText(R.string.notification_camera_is_active_content).toString()
        val builder = notificationBuilderTemplate
            .setContentText(notifyFmt.format(0))
        ctx.startForeground(1, builder.build())
    }

    fun update(scriptsReceived: Int) {
        val notifyFmt = ctx.getText(R.string.notification_camera_is_active_content).toString()
        val builder = notificationBuilderTemplate
            .setContentText(notifyFmt.format(scriptsReceived))
        notifyMan.notify(1, builder.build())
    }

    fun remove() {
        ctx.stopForeground(true)
    }

    private fun makeNotificationBuilder(): NotificationCompat.Builder {
        val chanId = "net.easimer.dcctl.notifychan.camserv"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyMan = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            makeNotificationChannel(notifyMan, chanId)
            return NotificationCompat.Builder(ctx, chanId)
        } else {
            return NotificationCompat.Builder(ctx)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeNotificationChannel(notifyMan: NotificationManager, chanId: String) {
        val maybeChannel = notifyMan.getNotificationChannel(chanId)
        if(maybeChannel == null) {
            val channel =
                NotificationChannel(chanId, chanId, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "DCCTL Service Channel"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            notifyMan.createNotificationChannel(channel)
        }
    }
}