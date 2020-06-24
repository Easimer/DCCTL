package net.easimer.dcctl.scripting.ui

import net.easimer.dcctl.scripting.Command

/**
 * Used by the CommandAdapter to tell the CommandViews how to delete themselves.
 */
class CommandDeleter(
    private val script : List<Command>,
    val callback : (Command) -> Unit
)