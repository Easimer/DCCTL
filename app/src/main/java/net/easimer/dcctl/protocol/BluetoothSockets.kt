package net.easimer.dcctl.protocol

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.InputStream
import java.util.*

private class BluetoothClientSocketImpl(private val sock : BluetoothSocket) : IClientSocket {
    override val inputStream: InputStream
        get() = sock.inputStream

    override fun close() {
        sock.close()
    }
}

private class BluetoothServerSocketImpl(private val sock : BluetoothServerSocket) : IServerSocket {
    override fun accept(): IClientSocket? {
        val csock = sock.accept()
        if(csock != null) {
            return BluetoothClientSocketImpl(csock)
        }

        return null
    }

    override fun close() {
        sock.close()
    }
}

fun createBluetoothServerSocket(ctx: Context): IServerSocket? {
    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    if (btAdapter != null) {
        val socket = btAdapter.listenUsingRfcommWithServiceRecord(
            "net.easimer.dcctl",
            UUID.fromString(BLUETOOTH_PROTOCOL_ID)
        )

        if(socket != null) {
            return BluetoothServerSocketImpl(socket)
        }
    }

    return null
}