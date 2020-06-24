package net.easimer.dcctl.protocol

import net.easimer.dcctl.utils.Event

const val BLUETOOTH_PROTOCOL_ID = "939a3570-763e-4ab2-a0de-94cd51759054"

open class BluetoothServerStatisticsListener {
    open fun onNumberOfScriptsReceivedChanged(numberOfScriptsReceived : Int) {}
}

interface IBluetoothServer {
    fun shutdown()
    val onScriptReceived : Event<Int>
}