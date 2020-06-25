package net.easimer.dcctl.camera

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.LifecycleService
import net.easimer.dcctl.*
import net.easimer.dcctl.protocol.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class CameraService : LifecycleService() {
    private val TAG = "CameraService"
    private val thread = HandlerThread("CameraServiceThread")

    private lateinit var handler : Handler

    private val totalScriptsReceived = AtomicInteger(0)

    private val servers : MutableList<ICommandServer> = LinkedList<ICommandServer>()
    private var controller: ICameraController? = null
    private lateinit var executor: ScriptExecutor
    private lateinit var notification : CameraServiceNotification

    override fun onCreate() {
        super.onCreate()

        notification = CameraServiceNotification(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val cfg = Configuration(this)

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

                val handlerProxy = object : IHandler {
                    override fun post(r: () -> Unit): Boolean {
                        return handler.post(r)
                    }
                }
                controller = it
                executor = ScriptExecutor(sfx, it, sleep, Log, handlerProxy)

                // Create the Bluetooth server
                val btSrvSock = createServerSocket(this, SERVER_KIND_BLUETOOTH)
                createServer(btSrvSock, executor)

                // Create the TCP server
                if(cfg.tcpServerEnabled) {
                    val tcpSrvSock = createServerSocket(this, SERVER_KIND_TCP)
                    createServer(tcpSrvSock, executor)
                }

                notification.create()

                // Display camera controller stats in the notification
                controller?.let {
                    it.onPictureTaken += { numberOfPicturesTaken ->
                        notification.update(
                            "camPicsTaken",
                            numberOfPicturesTaken,
                            R.string.notification_stat_pictures_taken
                        )
                    }
                    it.onBlinked += { numberOfTimesBlinked ->
                        notification.update(
                            "camTimesBlinked",
                            numberOfTimesBlinked,
                            R.string.notification_stat_times_blinked
                        )
                    }
                }
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

            servers.forEach {
                it.shutdown()
            }

            controller?.close()
            Log.d(TAG, "onDestroy on thread finished")
        }
        Log.d(TAG, "onDestroy: thread quiting")
        thread.quitSafely()
        Log.d(TAG, "onDestroy: thread quit")
        thread.join()
        Log.d(TAG, "onDestroy: thread joined")
    }

    private fun createServer(sock : IServerSocket?, executor: ScriptExecutor) {
        sock?.let {
            val srv = CommandServer(sock, executor)
            servers.add(srv)

            srv.onScriptReceived += {
                notification.update(
                    "btConn",
                    totalScriptsReceived.incrementAndGet(),
                    R.string.notification_stat_connections
                )
            }
        }
    }
}
