package net.easimer.dcctl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.easimer.dcctl.scripting.ui.ScriptActivity

const val MAIN_ACTIVITY_REQUEST_STOP_CAMERA_SERVICE = 1
const val MAIN_ACTIVITY_EXTRA_REQUEST = "Request"

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate")

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

        intent?.let {
            handleExtras(intent)
        }

        CameraServiceManager.addServiceEventListener(
            object : CameraServiceManager.IServiceManagementEventListener {
                private val activity = this@MainActivity

                override fun onServiceStarted() {
                    val svcBtn = findViewById<Button>(R.id.btnStartService)
                    svcBtn.isClickable = false
                    svcBtn.text = getText(R.string.stop_camera_service)
                    svcBtn.setOnClickListener {
                        CameraServiceManager.stopIfRunning(activity)
                    }
                }

                override fun onServiceStopped() {
                    val svcBtn = findViewById<Button>(R.id.btnStartService)
                    svcBtn.isClickable = true
                    svcBtn.text = getText(R.string.start_camera_service)
                    svcBtn.setOnClickListener {
                        CameraServiceManager.startIfDoesntExist(activity)
                    }
                }
            }, true
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            handleExtras(intent)
        }
    }

    fun onClickConfigMode(view: View) {
        startIntentOfType<ConfigActivity>()
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

    private fun handleExtras(intent: Intent) {
        intent.extras?.apply {
            val req = getInt(MAIN_ACTIVITY_EXTRA_REQUEST, -1)
            when (req) {
                MAIN_ACTIVITY_REQUEST_STOP_CAMERA_SERVICE ->
                    CameraServiceManager.stopIfRunning(this@MainActivity)
            }
        }
    }
}
