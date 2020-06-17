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
import net.easimer.dcctl.scripting.ScriptActivity

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
        startIntentOfType<ConfigActivity>()
    }

    fun onClickSensorDebug(view: View) {
        startIntentOfType<SensorDebug>()
    }

    fun onClickCameraService(view: View) {
        startIntentOfType<CameraServiceActivity>()
    }

    fun onClickScriptingMode(view: View) {
        startIntentOfType<ScriptActivity>()
    }

    fun onClickLogMode(view: View) {
        startIntentOfType<LogActivity>()
    }

    fun onClickPreferences(view: View) {
        startIntentOfType<SettingsActivity>()
    }

    private inline fun <reified T> startIntentOfType() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }
}
