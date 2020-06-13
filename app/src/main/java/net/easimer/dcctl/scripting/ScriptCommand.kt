package net.easimer.dcctl.scripting

import kotlinx.serialization.Serializable

@Serializable
sealed class ScriptCommand {
    @Serializable
    data class Wait(val time: Float) : ScriptCommand()
    @Serializable
    data class CaptureMultiple(val interval: Float, val count: Int) : ScriptCommand()
    @Serializable
    data class AudioSignal(val id : SoundEffect) : ScriptCommand()
}