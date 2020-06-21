package net.easimer.dcctl.protocol

const val BLUETOOTH_PROTOCOL_ID = "939a3570-763e-4ab2-a0de-94cd51759054"

open class BluetoothServerStatisticsListener {
    open fun onNumberOfScriptsReceivedChanged(numberOfScriptsReceived : Int) {}
}

interface IBluetoothServer {
    fun shutdown()
    fun addStatisticsListener(listener: BluetoothServerStatisticsListener)
    fun removeStatisticsListener(listener: BluetoothServerStatisticsListener)
}