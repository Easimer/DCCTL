package net.easimer.dcctl

interface ICameraCommandSource {
    fun take() : CameraControllerCommand
    fun shutdown() {}
}