package net.easimer.dcctl.protocol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import net.easimer.dcctl.Log
import net.easimer.dcctl.LogLevel
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.Serializer
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
                        val ser = Serializer(s.outputStream)
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

/**
 * Try to broadcast script to all paired Bluetooth devices that match the filter.
 * This function runs asynchronously on it's own thread.
 *
 * @param script The script to broadcast.
 * @param callback Called for every device we successfully sent the config to.
 * @param destinationFilter Filter function. Should return true if we should send the script to the
 * target device.
 */
fun broadcastScript(script: Script, callback: (success: Boolean, name: String) -> Unit, destinationFilter: (id: String) -> Boolean) {
    thread {
        val TAG = "BTBroadcastCfg"
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            val pairedDevices: Set<BluetoothDevice>? = btAdapter.bondedDevices
            pairedDevices?.forEach {
                if(destinationFilter(it.address)) {
                    sendConfigurationTo(it, script, callback)
                } else {
                    Log.d(TAG, "Skipped dest ${it.name} because it was excluded by the user", LogLevel.Note)
                }
            }
        } else {
            Log.d(TAG, "No btAdapter", LogLevel.Error)
        }
    }
}

fun forEachPairedBluetoothDevice(callback: (name: String, id: String) -> Unit) {
    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    if(btAdapter != null) {
        val pairedDevices: Set<BluetoothDevice>? = btAdapter.bondedDevices
        pairedDevices?.forEach {
            callback(it.name, it.address)
        }
    }
}