package net.easimer.dcctl.protocol

import net.easimer.dcctl.utils.Event

interface ICommandServer {
    fun shutdown()
    val onScriptReceived : Event<Int>
}