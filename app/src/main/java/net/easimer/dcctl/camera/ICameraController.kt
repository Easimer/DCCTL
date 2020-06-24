package net.easimer.dcctl.camera

import net.easimer.dcctl.utils.Event

interface ICameraController {
    fun close()
    fun takePicture(doFlash: Boolean)
    fun toggleFlash(enable : Boolean)

    val onPictureTaken : Event<Int>
    val onBlinked : Event<Int>
}