package net.easimer.dcctl

interface IConfigReceiver {
    fun pushCommand(cmd: CameraControllerCommand)
}