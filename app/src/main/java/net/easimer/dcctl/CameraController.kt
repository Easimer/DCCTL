package net.easimer.dcctl

import android.app.Activity
import android.hardware.Camera
import android.media.MediaPlayer
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

fun getCameraInstance(): Camera? {
    return try {
        Camera.open()
    } catch (e: Exception) {
        null
    }
}

class CameraController(val ctx: Activity, val cmdSrc : ICameraCommandSource) {
    val cam = getCameraInstance()

    val sfxReceived = MediaPlayer.create(ctx, R.raw.received001)
    val sfxTaken = MediaPlayer.create(ctx, R.raw.taken001)

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
        sfxReceived.start()
        Thread.sleep((config.delay * 1000).toLong())
        Log.d(TAG, "Post-delay")
        Log.d(TAG, "Interval: " + config.interval + " count: " + config.count)
        repeat(config.count) {
            Log.d(TAG, "Snap")
            snap()
            sfxTaken.start()
            Thread.sleep((config.interval * 1000).toLong())
        }
        Log.d(TAG, "Finished")
    }

    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "DCCTL"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("CameraController/IO", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Date())

        val path = "${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg"

        Log.d("CameraController/IO", "Path: ${path}")

        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File(path)
            }
            else -> null
        }
    }

    private val takePictureCallback = Camera.PictureCallback { data, cam ->
        val TAG = "CameraController/IO"
        val pictureFile: File = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: run {
            Log.d(TAG, ("Error creating media file, check storage permissions"))
            return@PictureCallback
        }

        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
            Log.d(TAG, "File saved")
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: ${e.message}")
        }

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
}