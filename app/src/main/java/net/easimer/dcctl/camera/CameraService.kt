package net.easimer.dcctl.camera

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import net.easimer.dcctl.*
import net.easimer.dcctl.protocol.IBluetoothServer
import net.easimer.dcctl.protocol.createBluetoothServer

class CameraService : Service() {
    private val TAG = "CameraService"
    private val thread = HandlerThread("CameraServiceThread")

    private lateinit var handler : Handler

    private var btSrv : IBluetoothServer? = null
    private var controller: ICameraController? = null
    private lateinit var executor: ScriptExecutor

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        thread.start()
        handler = Handler(thread.looper)

        handler.post {
            Log.d(TAG, "StartCommand on thread")
            tryCreatingController(this, handler) {
                controller = it
                executor = ScriptExecutor(this, it)
                btSrv = createBluetoothServer(this, executor)
            }
        }

        createNotification()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        removeNotification()

        Log.d(TAG, "onDestroy on UI")
        handler.post {
            Log.d(TAG, "onDestroy on thread")
            btSrv?.shutdown()
            controller?.close()
            Log.d(TAG, "onDestroy on thread finished")
        }
        Log.d(TAG, "onDestroy: thread quiting")
        thread.quitSafely()
        Log.d(TAG, "onDestroy: thread quit")
        thread.join()
        Log.d(TAG, "onDestroy: thread joined")
    }


    private fun createNotification() {
        val stopIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .let {
                it.putExtra(MAIN_ACTIVITY_EXTRA_REQUEST, MAIN_ACTIVITY_REQUEST_STOP_CAMERA_SERVICE)
                PendingIntent.getActivity(
                    this,
                    0,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
        }

        val actTerminate =
            NotificationCompat.Action.Builder(R.drawable.stop, getText(R.string.terminate_service), stopIntent)
                .build()
        val notification = makeNotificationBuilder()
            .setContentTitle(getText(R.string.notification_camera_is_active))
            .setSmallIcon(R.drawable.notification)
            .addAction(actTerminate)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun removeNotification() {
        stopForeground(true)
    }

    private fun makeNotificationBuilder(): NotificationCompat.Builder {
        val chanId = "net.easimer.dcctl.notifychan.camserv"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            makeNotificationChannel(notifyMan, chanId)
            return NotificationCompat.Builder(this, chanId)
        } else {
            return NotificationCompat.Builder(this)
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
