package net.easimer.dcctl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.core.content.getSystemService
import net.easimer.dcctl.scripting.SoundEffect

class AudioNotifications(ctx: Context) {
    // Contact the audio manager service
    private val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val sessionId = audioManager.generateAudioSessionId()

    // Build audio attribute
    private val builder = AudioAttributes.Builder()
    init {
        builder.setUsage(AudioAttributes.USAGE_MEDIA)
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    }
    private val audioAttr = builder.build()

    // Load the files
    private val sfxReceived = MediaPlayer.create(ctx, R.raw.received001, audioAttr, sessionId)
    private val sfxTaken = MediaPlayer.create(ctx, R.raw.taken001, audioAttr, sessionId)
    private val sfxKlaxon = MediaPlayer.create(ctx, R.raw.klaxon001, audioAttr, sessionId)
    private val sfxCombat = MediaPlayer.create(ctx, R.raw.ede001, audioAttr, sessionId)

    fun onCommandReceived() {
        playEffect(SoundEffect.Chirp)
    }

    fun onPictureTaken() {
        playEffect(SoundEffect.Blip)
    }

    fun playEffect(id : SoundEffect) {
        val sfx = when(id) {
            SoundEffect.Chirp -> sfxReceived
            SoundEffect.Blip -> sfxTaken
            SoundEffect.Klaxon -> sfxKlaxon
            SoundEffect.Combat -> sfxCombat
        }

        try {
            sfx.start()
        } catch(e: Exception) {
            e.printStackTrace()
        }

    }
}
