package net.easimer.dcctl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickCameraMode(view: View) {
        val btsrv = createBluetoothServer(this)

        if(btsrv != null) {
            if (btsrv.isOperational()) {
                createCameraActivity(this, btsrv)
            } else {
                Log.d(TAG, "Bluetooth server is not operational")
            }
        } else {
            Log.d(TAG, "Couldn't start bluetooth server")
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
}
