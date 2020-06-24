package net.easimer.dcctl.camera

import net.easimer.dcctl.IAudioNotifications
import net.easimer.dcctl.ILogger
import net.easimer.dcctl.Log
import net.easimer.dcctl.protocol.ICommandSink
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.ScriptCommand

class ScriptExecutor(
    private val sfx : IAudioNotifications,
    private val camera : ICameraController,
    private val sleep : IThreadSleep,
    private val log : ILogger
) : ICommandSink {
    private val TAG = "ScriptExecutor"

    override fun execute(script: Script) {
        sfx.onCommandReceived()

        script.commands.forEach {
            when(it) {
                is ScriptCommand.Wait -> execute(it)
                is ScriptCommand.CaptureMultiple -> execute(it)
                is ScriptCommand.AudioSignal -> execute(it)
                is ScriptCommand.Blink -> execute(it)
            }
        }
    }

    private fun execute(cmd: ScriptCommand.Wait) {
        log.d(TAG, "Wait for ${cmd.time} secs")
        sleep.sleep(cmd.time)
    }

    private fun execute(cmd: ScriptCommand.CaptureMultiple) {
        log.d(TAG, "Capture ${cmd.count} pics every ${cmd.interval} seconds")

        repeat(cmd.count) {
            sfx.onPictureTaken()
            camera.takePicture(cmd.flash)
            sleep.sleep(cmd.interval)
        }
    }

    private fun execute(cmd: ScriptCommand.AudioSignal) {
        log.d(TAG, "Playing sfx ${cmd.id}")
        sfx.playEffect(cmd.id)
    }

    private fun execute(cmd: ScriptCommand.Blink) {
        log.d(TAG, "Blink hold=${cmd.hold} secs")
        camera.toggleFlash(true)
        sleep.sleep(cmd.hold)
        camera.toggleFlash(false)
    }
}