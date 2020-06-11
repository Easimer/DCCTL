package net.easimer.dcctl

import android.content.Context
import android.media.MediaPlayer

class AudioNotifications(ctx: Context) {
    private val sfxReceived = MediaPlayer.create(ctx, R.raw.received001)
    private val sfxTaken = MediaPlayer.create(ctx, R.raw.taken001)

    fun onCommandReceived() {
        sfxReceived.start()
    }

    fun onPictureTaken() {
        sfxTaken.start()
    }
}
