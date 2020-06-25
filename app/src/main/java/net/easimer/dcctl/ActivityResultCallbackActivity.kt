package net.easimer.dcctl

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

/**
 * An AppCompatActivity class that can do permission requests and start activities for results
 * and receive the results in a callback, instead of using that stupid request code system.
 */
open class ActivityResultCallbackActivity : AppCompatActivity() {
    private val permCallbacks = HashMap<Int, (granted: List<String>) -> Unit>()
    private val intentCallbacks = HashMap<Int, () -> Unit>()
    private var nextRequestCode = 0

    /**
     * Request permissions then call back with the list of requests that got granted.
     * @param permissions List of permissions
     * @param callback Callback
     */
    fun requestPermissions(permissions: Array<String>, callback: (granted: List<String>) -> Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Filter out permissions that are already granted to us
            val permissionsToAskFor = permissions
                .map { Pair(it, ContextCompat.checkSelfPermission(this, it)) }
                .filter { it.second == PackageManager.PERMISSION_DENIED }
                .map { it.first }
                .toTypedArray()

            // List of permissions can't be empty
            if(permissionsToAskFor.isNotEmpty()) {
                val requestCode = getNextRequestCode()
                permCallbacks[requestCode] = callback
                requestPermissions(permissionsToAskFor, requestCode)
            } else {
                callback(permissions.toList())
            }
        } else {
            // No need to request them until API 23
            callback(permissions.toList())
        }
    }

    /**
     * Start an activity and call back when it's result arrives.
     * @param activity Identifier of the activity
     * @param callback Callback
     */
    fun startActivityForResult(activity : String, callback: () -> Unit) {
        val requestCode = getNextRequestCode()
        intentCallbacks[requestCode] = callback
        startActivityForResult(Intent(activity), requestCode)
    }

    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permCallbacks.get(requestCode)?.let {
            val granted = permissions
                .mapIndexed { k, v -> Pair(v, grantResults[k])}
                .filter { it.second == PackageManager.PERMISSION_GRANTED }
                .map { it.first }
            permCallbacks[requestCode]?.let {
                it(granted)
            }
            permCallbacks.remove(requestCode)
        }
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        intentCallbacks.get(requestCode)?.let {
            it()
            intentCallbacks.remove(requestCode)
        }
    }

    /**
     * Get the next request code.
     */
    private fun getNextRequestCode(): Int {
        val ret = nextRequestCode++
        // Only the bottom 16 bits can be used in a request code
        if(nextRequestCode > 0xFFFF) {
            nextRequestCode = 0
        }
        return ret
    }
}