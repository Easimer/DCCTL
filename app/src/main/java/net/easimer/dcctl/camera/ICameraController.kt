package net.easimer.dcctl.camera

interface ICameraController {
    fun close()
    fun takePicture(doFlash: Boolean)
    fun toggleFlash(enable : Boolean)

    fun addStatisticsListener(listener: CameraControllerStatisticsListener)
    fun removeStatisticsListener(listener: CameraControllerStatisticsListener)
}