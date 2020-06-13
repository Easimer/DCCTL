package net.easimer.dcctl.protocol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import net.easimer.dcctl.Log
import android.widget.Toast
import net.easimer.dcctl.scripting.ScriptDeserializer
import java.util.*
import kotlin.concurrent.thread

class BluetoothServer2(private val socket: BluetoothServerSocket, private val cmdSink: ICommandSink) {
    private val TAG = "BTSrv2"

    val serverThread = thread {
        var finish = false
        Log.d(TAG, "Listening on Bluetooth")
        while(!finish) {
            try {
                val csocket = socket.accept()
                if (csocket != null) {
                    Log.d(TAG, "New client")
                    try {
                        val ser = ScriptDeserializer(csocket.inputStream)
                        val script = ser.deserialize()
                        if(script != null) {
                            cmdSink.execute(script)
                        }
                        ser.close()
                    } catch (e: Exception) {
                        Log.d(TAG, "Client error: $e")
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

    fun shutdown() {
        Log.d(TAG, "closing socket")
        socket.close()
        Log.d(TAG, "joining thread")
        serverThread.join()
        Log.d(TAG, "joined thread")
    }
}

fun createBluetoothServer2(ctx: Context, cmdSink: ICommandSink): BluetoothServer2? {
    try {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            val socket = btAdapter.listenUsingRfcommWithServiceRecord(
                "net.easimer.dcctl",
                UUID.fromString(BLUETOOTH_PROTOCOL_ID)
            )
            return BluetoothServer2(socket, cmdSink)
        }
    } catch (e: Exception) {
        Toast.makeText(ctx, "Couldn't start Bluetooth server: " + e.localizedMessage, Toast.LENGTH_LONG).show()
    }

    return null
}