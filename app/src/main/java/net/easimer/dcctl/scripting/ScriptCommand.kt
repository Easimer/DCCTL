package net.easimer.dcctl.scripting

import kotlinx.serialization.Serializable

@Serializable
sealed class ScriptCommand {
    @Serializable
    class Wait(val time: Float) : ScriptCommand()
    @Serializable
    class CaptureMultiple(val interval: Float, val count: Int) : ScriptCommand()
    @Serializable
    class AudioSignal(val id : SoundEffect) : ScriptCommand()
}