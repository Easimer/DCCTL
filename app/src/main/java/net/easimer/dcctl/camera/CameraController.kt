package net.easimer.dcctl.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import net.easimer.dcctl.savePictureToMediaStorage


private class CameraController(
    private val ctx: Context,
    private val cameraDevice: CameraDevice,
    private val captureSession: CameraCaptureSession,
    private val imageReader: ImageReader,
    private val warmupImageReader: ImageReader,
    private val handler: Handler
) : ICameraController {
    private val TAG = "CameraController"

    init {
        imageReader.setOnImageAvailableListener( {
            val img = it.acquireNextImage()

            Log.d(TAG, "Received image, res: ${img.width}x${img.height}")
            val buf = img.planes[0].buffer
            val byteBuf = ByteArray(buf.remaining())
            buf.get(byteBuf)

            savePictureToMediaStorage(ctx, byteBuf)

            img.close()
        }, handler)

        warmupImageReader.setOnImageAvailableListener( {
            it.acquireNextImage().close()
        }, handler)

        createWarmupCaptureRequest()
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)

            Log.d(TAG, "Capture completed")
        }
    }

    private fun createCaptureRequest() {
        try {
            val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            builder.addTarget(imageReader.surface)

            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

            builder.set(CaptureRequest.JPEG_ORIENTATION, 0)

            captureSession.capture(builder.build(), captureCallback, handler)

        } catch(e : Exception) {
            e.printStackTrace()
        }
    }

    private fun createWarmupCaptureRequest() {
        try {
            val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            builder.addTarget(warmupImageReader.surface)

            captureSession.setRepeatingRequest(builder.build(), captureCallback, handler)

        } catch(e : Exception) {
            e.printStackTrace()
        }
    }

    override fun close() {
        captureSession.stopRepeating()
        captureSession.abortCaptures()
        captureSession.close()
        cameraDevice.close()
    }

    override fun takePicture() {
        createCaptureRequest()
    }
}

private fun getCamera(cameraManager: CameraManager): Pair<String, CameraCharacteristics>? {
    val id = cameraManager.cameraIdList.first {
        val char = cameraManager.getCameraCharacteristics(it)
        val facing = char.get(CameraCharacteristics.LENS_FACING)

        facing != null && facing == CameraCharacteristics.LENS_FACING_BACK
    }

    return if(id != null) {
        Pair(id, cameraManager.getCameraCharacteristics((id)))
    } else {
        null
    }
}

private fun createCaptureSession(
    imageReader: ImageReader,
    warmupImageReader: ImageReader,
    cameraDevice: CameraDevice,
    stateCallback: CameraCaptureSession.StateCallback,
    handler: Handler
) {
    val surfaces = listOf(imageReader.surface, warmupImageReader.surface)

    try {
        cameraDevice.createCaptureSession(surfaces, stateCallback, handler)
    } catch(e: CameraAccessException) {
        e.printStackTrace()
    }
}

fun tryCreatingController(ctx: Context, handler: Handler, callback: (controller: ICameraController) -> Unit) {
    val TAG = "tryCreatingController"
    var imageReader : ImageReader
    var warmupImageReader : ImageReader
    val cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    try {
        getCamera(cameraManager)?.let {
            val (id, char) = it
            val res = char.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            if(res != null) {
                Log.d(TAG, "Opening camera called $id with resolution $res")
                imageReader =
                    ImageReader.newInstance(res.width(), res.height(), ImageFormat.JPEG, 8)
                warmupImageReader =
                    ImageReader.newInstance(res.width(), res.height(), ImageFormat.YUV_420_888, 8)
                cameraManager.openCamera(id, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        Log.d(TAG, "Camera opened")
                        createCaptureSession(imageReader, warmupImageReader, camera, object : CameraCaptureSession.StateCallback() {
                            override fun onConfigureFailed(session: CameraCaptureSession) {}

                            override fun onConfigured(session: CameraCaptureSession) {
                                Log.d(TAG, "Camera is configured")
                                callback(CameraController(ctx, camera, session, imageReader, warmupImageReader, handler))
                            }
                        }, handler)
                    }

                    override fun onDisconnected(camera: CameraDevice) {}
                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.d(TAG, "Camera error $error")
                    }
                }, handler)
            }
        }
    } catch (sec : SecurityException) {
        Log.d(TAG, "SecurityException")
        throw sec
    } catch(e: Exception) {
        Log.d(TAG, "Exception: $e")
    }
}