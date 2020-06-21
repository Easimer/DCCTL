package net.easimer.dcctl.scripting.ui

import net.easimer.dcctl.scripting.ScriptCommand

interface ICommandView {
    fun bindTo(cmd: ScriptCommand)
}