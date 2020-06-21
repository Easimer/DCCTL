package net.easimer.dcctl.scripting.ui

import net.easimer.dcctl.scripting.ScriptCommand

/**
 * Used by the CommandAdapter to tell the CommandViews how to delete themselves.
 */
class CommandDeleter(
    private val script : List<ScriptCommand>,
    val callback : (ScriptCommand) -> Unit
)