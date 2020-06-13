package net.easimer.dcctl.camera

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.renderscript.Script
import android.util.Log
import net.easimer.dcctl.protocol.BluetoothServer2
import net.easimer.dcctl.protocol.createBluetoothServer2
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class CameraService : Service() {
    private val TAG = "CameraService"
    private val thread = HandlerThread("CameraServiceThread")

    private lateinit var handler : Handler

    private var btSrv :BluetoothServer2? = null
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
                executor = ScriptExecutor(it)
                btSrv = createBluetoothServer2(this, executor)
            }
        }

        return START_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.post {
            btSrv?.shutdown()
            controller?.close()
        }
        thread.quitSafely()
    }
}
