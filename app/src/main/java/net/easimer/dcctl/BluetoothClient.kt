package net.easimer.dcctl

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.Toast
import java.io.ObjectOutputStream
import java.util.*
import kotlin.concurrent.thread

private fun sendConfigurationTo(dev: BluetoothDevice, cfg: IConfigData, callback: (success: Boolean, name: String) -> Unit) {
    thread {
        val TAG = "BTBroadcastCfgDev"

        val s = dev.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_PROTOCOL_ID))
        if (s != null) {
            try {
                s.connect()
                if (s.isConnected()) {
                    Log.d(TAG, "Socket connected")
                    if (s.outputStream != null) {
                        Log.d(TAG, "Found server " + dev.name)
                        val oos = ObjectOutputStream(s.outputStream)
                        val packet =
                            BluetoothConfigPacket(cfg.delay, cfg.interval, cfg.count)
                        oos.writeObject(packet)
                        oos.close()
                        Log.d(TAG, "Sent packet")
                        callback(true, dev.name)
                    }
                } else {
                    Log.d(TAG, "Socket not connected")
                }
                s.close()
            } catch(e: Exception) {
                callback(false, dev.name)
            }
        } else {
            callback(false, dev.name)
        }
    }
    Log.d("ASD", "Thread Fired")
}

fun broadcastConfiguration(cfg: IConfigData, callback: (success: Boolean, name: String) -> Unit) {
    thread {
        val TAG = "BTBroadcastCfg"
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            val pairedDevices: Set<BluetoothDevice>? = btAdapter?.bondedDevices
            pairedDevices?.forEach {
                sendConfigurationTo(it, cfg, callback)
            }
        } else {
            Log.d(TAG, "No btAdapter")
        }
    }
}