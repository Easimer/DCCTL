package net.easimer.dcctl.protocol

import net.easimer.dcctl.scripting.Script

interface ICommandSink {
    fun execute(script: Script)
}