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
    private val stats = HashMap<String, NotificationStat>()

    fun create() {
        val notifyFmt = ctx.getText(R.string.notification_stat_connections).toString()
        val builder = notificationBuilderTemplate
        ctx.startForeground(1, builder.build())
    }

    fun update(key: String, value: Int, fmt: Int) {
        stats.put(key, NotificationStat(key, value, fmt))

        val contentText = stats
            .map { it.value }
            // Sort the stat entries by key
            .sortedBy { it.key }
            // Map the stat entry to the user readable text
            .map{ kv -> String.format(ctx.getText(kv.fmt).toString(), kv.value) }
            // Concatenate the formatted strings
            .reduce { lhs, rhs -> "${lhs}\n${rhs}" }

        val bigStyle = NotificationCompat.BigTextStyle()
            .bigText(contentText)

        val builder = notificationBuilderTemplate
            .setStyle(bigStyle)
            .setContentText("${contentText.substring(IntRange(0, 13))}...")
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

    data class NotificationStat(val key: String, val value: Int, val fmt: Int)
}