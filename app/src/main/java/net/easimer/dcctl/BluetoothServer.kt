package net.easimer.dcctl

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

data class BluetoothConfigPacket(
    override val delay: Float,
    override val interval: Float,
    override val count: Int
) : IConfigData, Serializable

const val BLUETOOTH_PROTOCOL_ID = "58937d4b-f6e0-40e9-93d3-c2b7826ed6d6"

class BluetoothServer(val socket: BluetoothServerSocket) : ICameraCommandSource {
    private val TAG = "BluetoothServer"
    private val cmdQueue : BlockingQueue<CameraControllerCommand> =
        LinkedBlockingQueue<CameraControllerCommand>()

    val serverThread = thread {
        var finish = false
        Log.d(TAG, "Listening on Bluetooth")
        while(!finish) {
            try {
                val csocket = socket.accept()
                if (csocket != null) {
                    Log.d(TAG, "New client")
                    try {
                        val ois = ObjectInputStream(csocket.inputStream)
                        val bcp = ois.readObject() as BluetoothConfigPacket
                        cmdQueue.put(CameraControllerCommand(false, bcp))
                    } catch (e: Exception) {
                        Log.d(TAG, "Client error")
                    } finally {
                        csocket.close()
                    }
                } else {
                    finish = true
                }
            } catch(e: Exception) {
                finish = true
            }
        }
        Log.d(TAG, "Server loop finished")
    }

    override fun shutdown() {
        cmdQueue.put(CameraControllerCommand(true, null))
        Log.d(TAG, "closing socket")
        socket.close()
        Log.d(TAG, "joining thread")
        serverThread.join()
        Log.d(TAG, "joined thread")
    }

    override fun take(): CameraControllerCommand {
        return cmdQueue.take()
    }

    fun isOperational(): Boolean {
        return true
    }
}

fun createBluetoothServer(ctx: Activity): BluetoothServer? {
    try {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            val socket = btAdapter.listenUsingRfcommWithServiceRecord(
                "net.easimer.dcctl",
                UUID.fromString(BLUETOOTH_PROTOCOL_ID)
            )
            return BluetoothServer(socket)
        }
    } catch (e: Exception) {
        Toast.makeText(ctx, "Couldn't start Bluetooth server: " + e.localizedMessage, Toast.LENGTH_LONG).show()
    }

    return null
}