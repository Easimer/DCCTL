package net.easimer.dcctl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

private fun getCameraInstance(): Camera? {
    return try {
        Camera.open()
    } catch (e: Exception) {
        null
    }
}

interface ICameraController {
    fun makePreviewView(ctx: Context) : View?

    fun interrupt()
}

private class CameraController(val ctx: Activity, val cmdSrc : ICameraCommandSource) : ICameraController {
    private val cam = getCameraInstance()

    private val sfx = AudioNotifications(ctx)

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

        releaseCamera()
        cmdSrc.shutdown()
        ctx.finish()
    }

    private fun executeConfig(config: IConfigData) {
        val TAG = "CameraController/exec"
        Log.d(TAG, "Pre-delay: " + config.delay + "s")
        sfx.onCommandReceived()
        Thread.sleep((config.delay * 1000).toLong())
        Log.d(TAG, "Post-delay")
        Log.d(TAG, "Interval: " + config.interval + " count: " + config.count)
        repeat(config.count) {
            Log.d(TAG, "Snap")
            snap()
            sfx.onPictureTaken()
            Thread.sleep((config.interval * 1000).toLong())
        }
        Log.d(TAG, "Finished")
    }

    private val takePictureCallback = Camera.PictureCallback { data, cam ->
        savePictureToMediaStorage(ctx, data)
        cam?.stopPreview()
        cam?.startPreview()
    }

    private fun snap() {
        val TAG = "CameraController/IO"
        Log.d(TAG, "Taking picture")
        try {
            cam?.takePicture(null, null, takePictureCallback)
        } catch(e: Exception) {
            Log.d(TAG, "Camera failure: ${e.message}")
        }
    }

    private fun releaseCamera() {
        cam?.release()
    }

    override fun makePreviewView(ctx: Context): View? {
        return cam?.let { CameraPreview(ctx, it) }
    }

    override fun interrupt() {
        thread.interrupt()
    }
}

// TODO(danielm): these should be in a CameraControllerCommon file or something tbh
const val CAMCTL_BACKEND_LEGACY = 1
const val CAMCTL_BACKEND_CAMERA2 = 2

fun createCameraController(ctx: Activity, cmdSrc : ICameraCommandSource, ver: Int): ICameraController {
    return when(ver) {
        1 -> CameraController(ctx, cmdSrc)
        else -> createCamera2Controller(ctx, cmdSrc)
    }
}