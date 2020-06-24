package net.easimer.dcctl.camera

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.LifecycleService
import net.easimer.dcctl.*
import net.easimer.dcctl.protocol.BluetoothServerStatisticsListener
import net.easimer.dcctl.protocol.IBluetoothServer
import net.easimer.dcctl.protocol.createBluetoothServer

class CameraService : LifecycleService() {
    private val TAG = "CameraService"
    private val thread = HandlerThread("CameraServiceThread")

    private lateinit var handler : Handler

    private var btSrv : IBluetoothServer? = null
    private var controller: ICameraController? = null
    private lateinit var executor: ScriptExecutor
    private lateinit var notification : CameraServiceNotification

    override fun onCreate() {
        super.onCreate()

        notification = CameraServiceNotification(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        thread.start()
        handler = Handler(thread.looper)

        handler.post {
            Log.d(TAG, "StartCommand on thread")
            tryCreatingController(this) {
                val sfx = AudioNotifications(this)
                val sleep = object : IThreadSleep {
                    override fun sleep(secs: Float) {
                        Thread.sleep((secs * 1000).toLong())
                    }
                }

                controller = it
                executor = ScriptExecutor(sfx, it, sleep, Log, handler)
                btSrv = createBluetoothServer(this, executor)
                notification.create()

                // Display network stats in the notification
                btSrv?.addStatisticsListener(object : BluetoothServerStatisticsListener() {
                    override fun onNumberOfScriptsReceivedChanged(numberOfScriptsReceived : Int) {
                        notification.update(
                            "btConn",
                            numberOfScriptsReceived,
                            R.string.notification_stat_connections
                        )
                    }
                })

                // Display camera controller stats in the notification
                controller?.addStatisticsListener(object : CameraControllerStatisticsListener() {
                    override fun onNumberOfPicturesTakenChanged(numberOfPicturesTaken: Int) {
                        notification.update(
                            "camPicsTaken",
                            numberOfPicturesTaken,
                            R.string.notification_stat_pictures_taken
                        )
                    }

                    override fun onNumberOfTimesBlinkedChanged(numberOfTimesBlinked: Int) {
                        notification.update(
                            "camTimesBlinked",
                            numberOfTimesBlinked,
                            R.string.notification_stat_times_blinked
                        )
                    }
                })
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        notification.remove()

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
}
