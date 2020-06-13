package net.easimer.dcctl.camera

import android.content.Context
import android.util.Log
import net.easimer.dcctl.AudioNotifications
import net.easimer.dcctl.protocol.ICommandSink
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.ScriptCommand

class ScriptExecutor(private val ctx : Context, private val camera : ICameraController) : ICommandSink {
    private val TAG = "ScriptExecutor"
    private val sfx = AudioNotifications(ctx)

    override fun execute(script: Script) {
        sfx.onCommandReceived()

        script.commands.forEach {
            when(it) {
                is ScriptCommand.Wait -> execute(it)
                is ScriptCommand.CaptureMultiple -> execute(it)
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
}