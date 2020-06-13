package net.easimer.dcctl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA), 1)
            }
        }
    }

    fun onClickConfigMode(view: View) {
        val intent = Intent(this, ConfigActivity::class.java)
        startActivity(intent)
    }

    fun onClickSensorDebug(view: View) {
        val intent = Intent(this, SensorDebug::class.java)
        startActivity(intent)
    }

    fun onClickCameraService(view: View) {
        val intent = Intent(this, CameraServiceActivity::class.java)
        startActivity(intent)
    }

    fun onClickScriptingMode(view: View) {

    }
}
