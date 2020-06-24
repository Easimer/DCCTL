package net.easimer.dcctl.protocol

import net.easimer.dcctl.Log
import net.easimer.dcctl.LogLevel
import net.easimer.dcctl.scripting.ScriptDeserializer
import net.easimer.dcctl.utils.Event
import kotlin.concurrent.thread

class CommandServer(
    private val sock : IServerSocket,
    private val cmdSink : ICommandSink
) : ICommandServer {
    private val TAG = "CmdSrv"
    override val onScriptReceived = Event<Int>()

    override fun shutdown() {
        sock.close()
        serverThread.join()
    }

    private val serverThread = thread {
        var finish = false
        var scriptsReceived = 0
        Log.d(TAG, "Serving requests")

        while(!finish) {
            try {
                val csocket = sock.accept()
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
                        Log.d(TAG, "Network error: ${e.message}", LogLevel.Error)
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
        Log.d(TAG, "Shutting down")
    }
}