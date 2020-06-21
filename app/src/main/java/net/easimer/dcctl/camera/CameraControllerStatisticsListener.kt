package net.easimer.dcctl.camera

open class CameraControllerStatisticsListener {
    open fun onNumberOfPicturesTakenChanged(numberOfPicturesTaken: Int) {}
    open fun onNumberOfTimesBlinkedChanged(numberOfTimesBlinked: Int) {}
}