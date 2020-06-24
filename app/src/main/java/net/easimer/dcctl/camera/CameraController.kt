package net.easimer.dcctl.camera

import android.content.Context
import android.os.Handler
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import net.easimer.dcctl.Log
import net.easimer.dcctl.LogLevel
import net.easimer.dcctl.savePictureToMediaStorage


private class CameraController(
    private val ctx: Context,
    private val camera: Camera,
    private val imageCapture: ImageCapture
) : ICameraController {
    private val TAG = "CameraController"
    private val stats = CameraControllerStatistics()

    override fun close() {
        // CameraX: no longer needed
    }

    override fun takePicture(doFlash: Boolean) {
        imageCapture.flashMode =
            if (doFlash) ImageCapture.FLASH_MODE_ON
            else ImageCapture.FLASH_MODE_OFF
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(ctx),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val buf = image.planes[0].buffer
                    val byteBuf = ByteArray(buf.remaining())
                    buf.get(byteBuf)

                    stats.onPictureTaken()
                    savePictureToMediaStorage(ctx, byteBuf)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.d(TAG, "takePicture failed: ${exception.message}", LogLevel.Error)
                }
            })
    }

    override fun toggleFlash(enable : Boolean) {
        camera.cameraControl.enableTorch(enable)

        if(enable) {
            stats.onBlinked()
        }
    }

    @Synchronized
    override fun addStatisticsListener(listener: CameraControllerStatisticsListener) {
        stats.addStatisticsListener(listener)
    }

    @Synchronized
    override fun removeStatisticsListener(listener: CameraControllerStatisticsListener) {
        stats.removeStatisticsListener(listener)
    }
}

fun tryCreatingController(ctx: Context, handler: Handler, callback: (controller: ICameraController) -> Unit) {
    val TAG = "tryCreatingController"

    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
    cameraProviderFuture.addListener(Runnable {
        val cameraProvider = cameraProviderFuture.get()
        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_90)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val camera = cameraProvider.bindToLifecycle(
            ctx as LifecycleOwner,
            cameraSelector,
            imageCapture
        )

        val ctl = CameraController(ctx, camera, imageCapture)

        callback(ctl)
    }, ContextCompat.getMainExecutor(ctx))

}