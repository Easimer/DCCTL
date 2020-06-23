package net.easimer.dcctl.camera

import net.easimer.dcctl.IAudioNotifications
import net.easimer.dcctl.Log
import net.easimer.dcctl.protocol.ICommandSink
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.ScriptCommand

class ScriptExecutor(private val sfx : IAudioNotifications, private val camera : ICameraController) : ICommandSink {
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
        Log.d(TAG, "Wait for ${cmd.time} secs")
        Thread.sleep((cmd.time * 1000).toLong())
    }

    private fun execute(cmd: ScriptCommand.CaptureMultiple) {
        Log.d(TAG, "Capture ${cmd.count} pics every ${cmd.interval} seconds")

        repeat(cmd.count) {
            sfx.onPictureTaken()
            camera.takePicture()
            Thread.sleep((cmd.interval * 1000).toLong())
        }
    }

    private fun execute(cmd: ScriptCommand.AudioSignal) {
        Log.d(TAG, "Playing sfx ${cmd.id}")
        sfx.playEffect(cmd.id)
    }

    private fun execute(cmd: ScriptCommand.Blink) {
        Log.d(TAG, "Blink hold=${cmd.hold} secs")
        camera.toggleFlash(true)
        Thread.sleep((cmd.hold * 1000).toLong())
        camera.toggleFlash(false)
    }
}