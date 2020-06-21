package net.easimer.dcctl.camera

import java.util.*

class CameraControllerStatistics {
    private val statListeners = LinkedList<CameraControllerStatisticsListener>()
    private var numberOfPicturesTaken = 0
    private var numberOfTimesBlinked = 0

    @Synchronized
    fun addStatisticsListener(listener: CameraControllerStatisticsListener) {
        statListeners.add(listener)
    }

    @Synchronized
    fun removeStatisticsListener(listener: CameraControllerStatisticsListener) {
        statListeners.remove(listener)
    }

    fun onPictureTaken() {
        numberOfPicturesTaken++

        statListeners.forEach {
            it.onNumberOfPicturesTakenChanged(numberOfPicturesTaken)
        }
    }

    fun onBlinked() {
        numberOfTimesBlinked++

        statListeners.forEach {
            it.onNumberOfTimesBlinkedChanged(numberOfTimesBlinked)
        }
    }
}