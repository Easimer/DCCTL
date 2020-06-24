package net.easimer.dcctl.scripting

import kotlinx.serialization.Serializable

@Serializable
class Script(val commands : List<Command>)