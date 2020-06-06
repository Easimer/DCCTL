package net.easimer.dcctl

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlin.concurrent.thread

class CameraController(val ctx: Activity, val cmdSrc : ICameraCommandSource) {
    val thread  = thread {
        val TAG = "CameraControllerThread"
        var finish = false

        Log.d(TAG, "Entering main loop")

        while(!finish) {
            try {
                val cmd = cmdSrc.take()
                finish = cmd.finish
                if(!cmd.finish) {
                    if(cmd.config != null) {
                        executeConfig(cmd.config)
                    }
                }
            } catch (e: InterruptedException) {
                finish = true
            }
        }

        Log.d(TAG, "Exited main loop")

        cmdSrc.shutdown()
        ctx.finish()
    }

    private fun executeConfig(config: IConfigData) {
        val TAG = "CameraController/exec"
        // TODO(easimer): access camera
        Log.d(TAG, "Pre-delay: " + config.delay + "s")
        Thread.sleep((config.delay * 1000).toLong())
        Log.d(TAG, "Post-delay")
        Log.d(TAG, "Interval: " + config.interval + " count: " + config.count)
        repeat(config.count) {
            Log.d(TAG, "Snap")
            Thread.sleep((config.interval * 1000).toLong())
        }
        Log.d(TAG, "Finished")
    }
}