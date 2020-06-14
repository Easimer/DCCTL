package net.easimer.dcctl.scripting

import kotlinx.serialization.Serializable

@Serializable
sealed class ScriptCommand {
    @Serializable
    data class Wait(var time: Float) : ScriptCommand()
    @Serializable
    data class CaptureMultiple(var interval: Float, var count: Int) : ScriptCommand()
    @Serializable
    data class AudioSignal(var id : SoundEffect) : ScriptCommand()
    @Serializable
    data class Blink(var hold: Float) : ScriptCommand()
}