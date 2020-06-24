package net.easimer.dcctl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import net.easimer.dcctl.camera.ICameraController
import net.easimer.dcctl.camera.IThreadSleep
import net.easimer.dcctl.camera.ScriptExecutor
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.Command
import net.easimer.dcctl.scripting.SoundEffect
import org.junit.Test

/**
 * Tests for the ScriptExecutor class.
 */
class ScriptExecutorTests {
    @Test
    fun waitTest() {
        val (exec, sfx, ctl, sleep, logger) = createExecutor()

        val cmd = Command.Wait(5.0f)
        val script = Script(listOf(cmd))

        exec.execute(script)

        verify {
            sfx.onCommandReceived()
            sleep.sleep(5.0f)
        }
    }

    @Test
    fun testCaptureMultiple() {
        val (exec, sfx, ctl, sleep, logger) = createExecutor()

        val cmd = Command.CaptureMultiple(1.0f, 3, false)
        val script = Script(listOf(cmd))

        exec.execute(script)

        verify(exactly = 3) { sfx.onPictureTaken() }
        verify(exactly = 3) { sleep.sleep(1.0f) }
        verify(exactly = 3) { ctl.takePicture(false) }
    }

    @Test
    fun testAudioSignal() {
        val (exec, sfx, ctl, sleep, logger) = createExecutor()

        val cmd = Command.AudioSignal(SoundEffect.Klaxon)
        val script = Script(listOf(cmd))

        exec.execute(script)

        verify { sfx.playEffect(SoundEffect.Klaxon) }
    }

    @Test
    fun testBlink() {
        val (exec, sfx, ctl, sleep, logger) = createExecutor()

        val cmd = Command.Blink(2.4f)
        val script = Script(listOf(cmd))

        exec.execute(script)

        verifyOrder {
            ctl.toggleFlash(true)
            sleep.sleep(2.4f)
            ctl.toggleFlash(false)
        }
    }

    data class ExecutorContext(
        val exec: ScriptExecutor,
        val sfx : IAudioNotifications,
        val ctl : ICameraController,
        val sleep : IThreadSleep,
        val logger : ILogger)

    private fun createExecutor() : ExecutorContext {
        val sleep = mockk<IThreadSleep>()
        val sfx = mockk<IAudioNotifications>()
        val ctl = mockk<ICameraController>()
        val logger = mockk<ILogger>()

        every { sfx.onCommandReceived() } returns Unit
        every { sfx.onPictureTaken() } returns Unit
        every { sfx.playEffect(any()) } returns Unit
        every { sleep.sleep(any()) } returns Unit
        every { logger.d(any(), any(), any()) } returns Unit
        every { ctl.takePicture(any()) } returns Unit
        every { ctl.toggleFlash(any()) } returns Unit

        val exec = ScriptExecutor(sfx, ctl, sleep, logger)

        return ExecutorContext(exec, sfx, ctl, sleep, logger)
    }
}