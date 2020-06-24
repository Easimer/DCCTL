package net.easimer.dcctl.protocol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import net.easimer.dcctl.Log
import android.widget.Toast
import net.easimer.dcctl.LogLevel
import net.easimer.dcctl.scripting.ScriptDeserializer
import net.easimer.dcctl.utils.Event
import java.util.*
import kotlin.concurrent.thread

private class BluetoothServer(private val socket: BluetoothServerSocket, private val cmdSink: ICommandSink) : IBluetoothServer {
    private val TAG = "BTSrv2"
    override val onScriptReceived = Event<Int>()

    val serverThread = thread {
        var finish = false
        var scriptsReceived = 0
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
                            // Notify listeners about this script we received
                            scriptsReceived++
                            onScriptReceived(scriptsReceived)

                            cmdSink.execute(script)
                        }
                        ser.close()
                    } catch (e: Exception) {
                        Log.d(TAG, "Client error: ${e.message}", LogLevel.Error)
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
        Log.d(TAG, "closing socket")
        socket.close()
        Log.d(TAG, "joining thread")
        serverThread.join()
        Log.d(TAG, "joined thread")
    }
}

fun createBluetoothServer(ctx: Context, cmdSink: ICommandSink): IBluetoothServer? {
    try {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            val socket = btAdapter.listenUsingRfcommWithServiceRecord(
                "net.easimer.dcctl",
                UUID.fromString(BLUETOOTH_PROTOCOL_ID)
            )
            return BluetoothServer(socket, cmdSink)
        }
    } catch (e: Exception) {
        Toast.makeText(ctx, "Couldn't start Bluetooth server: " + e.localizedMessage, Toast.LENGTH_LONG).show()
    }

    return null
}