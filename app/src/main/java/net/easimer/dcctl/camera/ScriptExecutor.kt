package net.easimer.dcctl.camera

import android.os.Handler
import net.easimer.dcctl.IAudioNotifications
import net.easimer.dcctl.ILogger
import net.easimer.dcctl.protocol.ICommandSink
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.Command
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class ScriptExecutor(
    private val sfx : IAudioNotifications,
    private val camera : ICameraController,
    private val sleep : IThreadSleep,
    private val log : ILogger,
    private val handler : Handler
) : ICommandSink {
    private val TAG = "ScriptExecutor"
    private val cmdQueue = LinkedBlockingQueue<Command>()

    @Synchronized
    override fun execute(script: Script) {
        // Enqueue commands
        script.commands.forEach {
            cmdQueue.put(it)
        }

        // Pump the command queue in the future
        handler.post {
            sfx.onCommandReceived()
            val temp = LinkedList<Command>()

            cmdQueue.drainTo(temp)

            temp.forEach {
                when(it) {
                    is Command.Wait -> execute(it)
                    is Command.CaptureMultiple -> execute(it)
                    is Command.AudioSignal -> execute(it)
                    is Command.Blink -> execute(it)
                }
            }
        }
    }

    private fun execute(cmd: Command.Wait) {
        log.d(TAG, "Wait for ${cmd.time} secs")
        sleep.sleep(cmd.time)
    }

    private fun execute(cmd: Command.CaptureMultiple) {
        log.d(TAG, "Capture ${cmd.count} pics every ${cmd.interval} seconds")

        repeat(cmd.count) {
            sfx.onPictureTaken()
            camera.takePicture(cmd.flash)
            sleep.sleep(cmd.interval)
        }
    }

    private fun execute(cmd: Command.AudioSignal) {
        log.d(TAG, "Playing sfx ${cmd.id}")
        sfx.playEffect(cmd.id)
    }

    private fun execute(cmd: Command.Blink) {
        log.d(TAG, "Blink hold=${cmd.hold} secs")
        camera.toggleFlash(true)
        sleep.sleep(cmd.hold)
        camera.toggleFlash(false)
    }
}