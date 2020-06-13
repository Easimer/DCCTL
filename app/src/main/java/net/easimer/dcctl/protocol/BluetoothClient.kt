package net.easimer.dcctl.protocol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import net.easimer.dcctl.Log
import net.easimer.dcctl.LogLevel
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.ScriptSerializer
import java.util.*
import kotlin.concurrent.thread

private fun sendConfigurationTo(dev: BluetoothDevice, script: Script, callback: (success: Boolean, name: String) -> Unit) {
    thread {
        val TAG = "BTBroadcastScriptDev"

        val s = dev.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_PROTOCOL_ID))
        if (s != null) {
            try {
                s.connect()
                if (s.isConnected()) {
                    Log.d(TAG, "Socket connected")
                    if (s.outputStream != null) {
                        Log.d(TAG, "Found server " + dev.name)
                        val ser = ScriptSerializer(s.outputStream)
                        ser.serialize(script)
                        ser.close()
                        Log.d(TAG, "Sent packet")
                        callback(true, dev.name)
                    }
                } else {
                    Log.d(TAG, "Socket not connected", LogLevel.Error)
                }
                s.close()
            } catch(e: Exception) {
                e.printStackTrace()
                callback(false, dev.name)
            }
        } else {
            callback(false, dev.name)
        }
    }
}

fun broadcastScript(script: Script, callback: (success: Boolean, name: String) -> Unit) {
    thread {
        val TAG = "BTBroadcastCfg"
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            val pairedDevices: Set<BluetoothDevice>? = btAdapter.bondedDevices
            pairedDevices?.forEach {
                sendConfigurationTo(it, script, callback)
            }
        } else {
            Log.d(TAG, "No btAdapter", LogLevel.Error)
        }
    }
}