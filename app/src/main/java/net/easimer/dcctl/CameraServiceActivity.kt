package net.easimer.dcctl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.easimer.dcctl.camera.CameraService

class CameraServiceActivity : AppCompatActivity() {
    private var serviceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_service)

        Intent(this, CameraService::class.java).also {
            startService(it)
            serviceIntent = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(serviceIntent != null) {
            stopService(serviceIntent)
        }
    }
}
