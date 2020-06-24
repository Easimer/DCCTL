package net.easimer.dcctl.scripting

import kotlinx.serialization.Serializable

@Serializable
sealed class Command {
    @Serializable
    data class Wait(var time: Float) : Command()
    @Serializable
    data class CaptureMultiple(
        var interval: Float,
        var count: Int,
        var flash: Boolean
    ) : Command()
    @Serializable
    data class AudioSignal(var id : SoundEffect) : Command()
    @Serializable
    data class Blink(var hold: Float) : Command()
}