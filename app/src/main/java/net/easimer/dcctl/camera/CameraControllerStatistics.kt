package net.easimer.dcctl.camera

import net.easimer.dcctl.utils.Event
import java.util.*

class CameraControllerStatistics {
    private var numberOfPicturesTaken = 0
    private var numberOfTimesBlinked = 0
    val onPictureTaken = Event<Int>()
    val onBlinked = Event<Int>()

    fun pictureTaken() {
        numberOfPicturesTaken++
        onPictureTaken(numberOfPicturesTaken)
    }

    fun blinked() {
        numberOfTimesBlinked++
        onBlinked(numberOfTimesBlinked)
    }
}