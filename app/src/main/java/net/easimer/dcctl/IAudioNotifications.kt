package net.easimer.dcctl

import net.easimer.dcctl.scripting.SoundEffect

interface IAudioNotifications {
    fun onCommandReceived()
    fun onPictureTaken()
    fun playEffect(id : SoundEffect)
}