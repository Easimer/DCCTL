package net.easimer.dcctl

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class CameraPreview(
    context: Context,
    private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback {
    val TAG = "CameraPreview"

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
        // setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mCamera.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d(TAG, "Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (mHolder.surface == null) {
            return
        }

        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
        }

        mCamera.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: ${e.message}")
            }
        }
    }
}
