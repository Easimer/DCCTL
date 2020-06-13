package net.easimer.dcctl.camera

interface ICameraController {
    fun close()
    fun takePicture()
}