package net.easimer.dcctl.scripting

import kotlinx.serialization.Serializable

@Serializable
enum class SoundEffect {
    Chirp,
    Blip,
    Klaxon,
    Combat,
}