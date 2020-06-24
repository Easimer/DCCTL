package net.easimer.dcctl

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import net.easimer.dcctl.databinding.ActivityConfigBinding
import net.easimer.dcctl.protocol.broadcastScript
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.Command

class ConfigActivity : AppCompatActivity() {
    val vm = ConfigViewModel(10.0f, 1.0f, 0, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityConfigBinding = DataBindingUtil.setContentView(this, R.layout.activity_config)
        binding.config = vm
    }

    fun onPushRemoteClick(view: View) {
        val cmdWait = Command.Wait(vm.delay)
        val cmdCapture = Command.CaptureMultiple(vm.interval, vm.count, vm.flash)
        val script = Script(listOf(cmdWait, cmdCapture))
        val excludedDevicesDefault = HashSet<String>()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val excludedDevicesPref = pref.getStringSet("excluded_devices", excludedDevicesDefault)
        val excludedDevices =
            if (excludedDevicesPref != null) excludedDevicesPref else excludedDevicesDefault

        broadcastScript(script, { success, name ->
            if (success) {
                runOnUiThread {
                    val toast = Toast.makeText(
                        this,
                        "Pushed configuration to $name",
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                }
            }
        }, { id -> !excludedDevices.contains(id) })
    }
}
