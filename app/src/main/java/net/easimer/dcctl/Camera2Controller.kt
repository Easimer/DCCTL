package net.easimer.dcctl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

// TODO(danielm): auto-exposure doesn't work. we need to make AE converge somehow

private fun pickCameraDevice(ctx: Activity, ctl: Camera2ControllerBuilder): Boolean {
    val TAG = "pickCameraDevice"

    Log.d(TAG, "Picking a device")
    val camMgmt =
        ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    val backCamId = camMgmt.cameraIdList.filter {
        val char = camMgmt.getCameraCharacteristics(it)
        val facing = char.get(CameraCharacteristics.LENS_FACING);

        facing != null && facing == CameraCharacteristics.LENS_FACING_BACK
    }.first()

    if(backCamId != null) {
        Log.d(TAG, "Picking a device: ${backCamId}")

        try {
            val char = camMgmt.getCameraCharacteristics(backCamId)
            val size = char.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            if(size != null) {
                ctl.size = size
                camMgmt.openCamera(backCamId, ctl, ctl.handler)
                Log.d("pickCameraDevice", "openCamera succeeded, size=${size}")
                return true
            } else {
                Log.d(TAG, "Couldn't determine sensor active array size")
                return false
            }
        } catch (e: SecurityException) {
            Log.d("pickCameraDevice", "openCamera failed: SecurityException: ${e.message}")
            return false
        }
    } else {
        Log.d("pickCameraDevice", "No back facing camera!")
        return false
    }
}

private class CaptureCaptureCallback(private val ctl: Camera2Controller) : CameraCaptureSession.CaptureCallback() {
    private val TAG = "CaptureCaptureCallback"
    private var state = 0

    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest, result: TotalCaptureResult
    ) {
        ctl.allowImageWrite()
    }
}

private class CaptureStateCallback(val ctl: Camera2ControllerBuilder) : CameraCaptureSession.StateCallback() {
    private val TAG = "CaptureStateCallback"

    override fun onConfigured(session: CameraCaptureSession) {
        Log.d(TAG, "Camera has been configured")
        ctl.onConfigured(session)
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        Log.d(TAG, "Camera has NOT been configured!")
        ctl.onConfigured()
    }
}

private class Camera2ControllerBuilder(val ctx: Activity, val cmdSrc: ICameraCommandSource) : CameraDevice.StateCallback() {
    private val TAG = "Cam2CtlBldr"
    var size: Rect = Rect(0, 0, 0, 0)
    private var instance : Camera2Controller? = null
    private val sema = Semaphore(0)
    private val handlerThread = HandlerThread("Camera2ControllerBuilderThread")
    val handler : Handler
    private var reader: ImageReader? = null

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        Log.d(TAG, "Handler thread should be running")
    }

    override fun onOpened(dev: CameraDevice) {
        Log.d(TAG, "Camera has been opened")
        // create capture session
        Log.d(TAG, "Size ${size}")
        val reader = ImageReader.newInstance(size.width(), size.height(), ImageFormat.JPEG, 2)
        this.reader = reader
        val surfArr = arrayOf(reader.surface)
        val session = dev.createCaptureSession(
            MutableList(surfArr.size, { i -> surfArr[i] }),
            CaptureStateCallback(this),
            handler
        )
    }

    override fun onDisconnected(dev: CameraDevice) {
        Log.d(TAG, "Camera has been disconnected")
    }

    override fun onError(camera: CameraDevice, error: Int) {
        Log.d(TAG, "device error code: ${error}")
    }

    fun onConfigured(captureSession: CameraCaptureSession) {
        reader?.also {
            instance = Camera2Controller(handler, handlerThread, it, captureSession, ctx, cmdSrc)
        }
        Log.d(TAG, "Releasing semaphore")
        sema.release()
    }

    fun onConfigured() {
    }

    fun getController() : ICameraController? {
        Log.d(TAG, "Acquiring semaphore")
        sema.acquire()
        return instance
    }
}

private class PictureWriter(private val ctx: Context) : ImageReader.OnImageAvailableListener {
    var allowWrite = false

    override fun onImageAvailable(reader: ImageReader?) {
        if(reader != null) {
            val img = reader.acquireNextImage()
            if(allowWrite) {
                val buf = img.planes[0].buffer
                val bytebuf = ByteArray(buf.remaining())
                buf.get(bytebuf)

                savePictureToMediaStorage(ctx, bytebuf)
                allowWrite = false
            }

            img.close()
        }
    }
}

private class Camera2Controller(
    private val handler : Handler,
    private val handlerThread: HandlerThread,
    private val reader: ImageReader,
    private val captureSession: CameraCaptureSession,
    private val ctx: Activity,
    private val cmdSrc: ICameraCommandSource
) : ICameraController {
    private val TAG = "Camera2Ctl"
    private val device = captureSession.device
    private val sfx = AudioNotifications(ctx)

    private val diskIOHandlerThread = HandlerThread("Cam2DiskIOHandlerThread")
    private val diskIOHandler : Handler

    private val captureListener = CaptureCaptureCallback(this)

    init {
        diskIOHandlerThread.start()
        diskIOHandler = Handler(diskIOHandlerThread.looper)
    }

    private val pictureWriter = PictureWriter(ctx)

    init {
        reader.setOnImageAvailableListener(pictureWriter, diskIOHandler)
    }

    override fun makePreviewView(ctx: Context): View? {
        return null
    }

    override fun interrupt() {
        thread.interrupt()
    }

    val thread = thread {
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

        captureSession.close()
        handlerThread.quitSafely()
        diskIOHandlerThread.quitSafely()
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

    private val reqBuilder =
        device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
    private val reqBuilderPreview =
        device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

    private fun snap() {
        reqBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        reqBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        reqBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        reqBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
        reqBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START)

        reqBuilder.addTarget(reader.surface)

        captureSession.capture(reqBuilder.build(), captureListener, handler)
    }

    fun allowImageWrite() {
        pictureWriter.allowWrite = true
    }
}

fun createCamera2Controller(ctx: Activity, cmdSrc : ICameraCommandSource): ICameraController {
    val TAG = "createCam2Ctl"
    val ctlb = Camera2ControllerBuilder(ctx, cmdSrc)

    if(!pickCameraDevice(ctx, ctlb)) {
        throw Exception()
    }

    val ctl = ctlb.getController()
    if(ctl != null) {
        return ctl
    } else {
        throw Exception()
    }
}