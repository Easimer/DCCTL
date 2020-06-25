package net.easimer.dcctl

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import net.easimer.dcctl.scripting.ui.ScriptActivity

const val MAIN_ACTIVITY_REQUEST_STOP_CAMERA_SERVICE = 1
const val MAIN_ACTIVITY_EXTRA_REQUEST = "Request"

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    private lateinit var permReq : PermissionRequester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permReq = PermissionRequester(this)

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
                        requestPermissionsAndStartService()
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

    private fun onBluetoothEnableRequestFinished() {
        // When this is called we have already asked the user to enable Bluetooth
        // We don't know if Bluetooth has been actually turned on so we query the default adapter
        BluetoothAdapter.getDefaultAdapter()?.let {
            if(it.isEnabled) {
                // User enabled it, hooray
                CameraServiceManager.startIfDoesntExist(this)
            } else {
                // User didn't enable it
                // Tell the user why we need Bluetooth to be turned on
                AlertDialog.Builder(this)
                    .setTitle(R.string.bluetooth_dialog_title)
                    .setMessage(R.string.bluetooth_dialog_message)
                    .setPositiveButton(R.string.bluetooth_dialog_retry) { _, _ ->
                        // User clicked "retry", show the BT dialog again
                        permReq.startActivityForResult(BluetoothAdapter.ACTION_REQUEST_ENABLE, { onBluetoothEnableRequestFinished() })
                    }
                    .setNegativeButton(R.string.bluetooth_dialog_ok) { _, _ ->
                        // User acknowledged the fact that we can't function without bluetooth
                    }
                    .show()
            }
        }
    }

    private fun onCameraPermissionsRequestFinished(granted: Boolean) {
        if(granted) {
            // Once we have camera permissions, check whether Bluetooth is turned on
            BluetoothAdapter.getDefaultAdapter()?.let {
                if (it.isEnabled) {
                    CameraServiceManager.startIfDoesntExist(this)
                } else {
                    // Bluetooth is off, ask user to enable it
                    permReq.startActivityForResult(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE,
                        { onBluetoothEnableRequestFinished() })
                }
            }
        } else {
            // User denied the camera permission request
            AlertDialog.Builder(this)
                .setTitle(R.string.camera_permission_dialog_title)
                .setMessage(R.string.camera_permission_dialog_message)
                .setPositiveButton(R.string.camera_permission_dialog_retry) { _, _ ->
                    // User clicked "retry", try asking for camera perms again
                    permReq.requestPermissions(arrayOf(Manifest.permission.CAMERA)) { granted ->
                        onCameraPermissionsRequestFinished(
                            Manifest.permission.CAMERA in granted
                        )
                    }
                }
                .setNegativeButton(R.string.camera_permission_dialog_yes) { _, _ ->
                    // User acknowledged the fact that we can't function without the camera
                }
                .show()
        }
    }

    private fun requestPermissionsAndStartService() {
        permReq.requestPermissions(arrayOf(Manifest.permission.CAMERA)) { granted ->
            onCameraPermissionsRequestFinished(Manifest.permission.CAMERA in granted)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permReq.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permReq.onActivityResult(requestCode, resultCode, data)
    }
}
