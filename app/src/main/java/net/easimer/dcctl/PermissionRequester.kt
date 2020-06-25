package net.easimer.dcctl

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.*

// TODO(easimer): We could subclass Activity (like a PermissionCallbackActivity or smthn),
//  overriding requestPermissions and onRequestPermissionsResult and make MainActivity a
//  PermissionCallbackActivity
class PermissionRequester(private val ctx: Activity) {
    private val permCallbacks = HashMap<Int, (granted: List<String>) -> Unit>()
    private val intentCallbacks = HashMap<Int, () -> Unit>()
    private var nextRequestCode = 0

    fun requestPermissions(permissions: Array<String>, callback: (granted: List<String>) -> Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Filter out permissions that are already granted to us
            val permissionsToAskFor = permissions
                .map { Pair(it, ContextCompat.checkSelfPermission(ctx, it)) }
                .filter { it.second == PackageManager.PERMISSION_DENIED }
                .map { it.first }
                .toTypedArray()

            // List of permissions can't be empty
            if(permissionsToAskFor.isNotEmpty()) {
                val requestCode = getNextRequestCode()
                permCallbacks[requestCode] = callback
                ctx.requestPermissions(permissionsToAskFor, requestCode)
            } else {
                callback(permissions.toList())
            }
        } else {
            // No need to request them until API 23
            callback(permissions.toList())
        }
    }

    fun startActivityForResult(intent : String, callback: () -> Unit) {
        val requestCode = getNextRequestCode()
        intentCallbacks[requestCode] = callback
        ctx.startActivityForResult(Intent(intent), requestCode)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("PermReq", "Le activity result for request $requestCode has arrived: $resultCode")
        intentCallbacks.get(requestCode)?.let {
            it()
            intentCallbacks.remove(requestCode)
        }
    }

    private fun getNextRequestCode(): Int {
        val ret = nextRequestCode++
        // Only the bottom 16 bits can be used in a request code
        if(nextRequestCode > 0xFFFF) {
            nextRequestCode = 0
        }
        return ret
    }
}