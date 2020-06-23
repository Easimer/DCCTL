package net.easimer.dcctl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.easimer.dcctl.camera.ICameraController
import net.easimer.dcctl.camera.IThreadSleep
import net.easimer.dcctl.camera.ScriptExecutor
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.ScriptCommand
import org.junit.Test

/**
 * Tests for the ScriptExecutor class.
 */
class ScriptExecutorTests {
    @Test
    fun waitTest() {
        val sleep = mockk<IThreadSleep>()
        val sfx = mockk<IAudioNotifications>()
        val ctl = mockk<ICameraController>()
        val logger = mockk<ILogger>()

        every {
            sfx.onCommandReceived()
        } returns Unit
        every {
            sleep.sleep(any())
        } returns Unit
        every {
            logger.d(any(), any(), any())
        } returns Unit

        val exec = ScriptExecutor(sfx, ctl, sleep, logger)
        val cmd = ScriptCommand.Wait(5.0f)
        val script = Script(listOf(cmd))

        exec.execute(script)

        verify {
            sfx.onCommandReceived()
            sleep.sleep(5.0f)
        }
    }
}