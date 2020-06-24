package net.easimer.dcctl.scripting.ui

import net.easimer.dcctl.scripting.Command

interface ICommandView {
    fun bindTo(cmd: Command)
}