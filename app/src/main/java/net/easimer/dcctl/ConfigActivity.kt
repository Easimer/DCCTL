package net.easimer.dcctl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import net.easimer.dcctl.databinding.ActivityConfigBinding
import java.util.*
import kotlin.collections.HashMap

const val BUNDLE_CONFIG_DELAY = "CfgDelay"
const val BUNDLE_CONFIG_INTERVAL = "CfgInterval"
const val BUNDLE_CONFIG_COUNT = "CfgCount"

val gCommandSourceStorage = HashMap<String, ICameraCommandSource>()

class ConfigActivity : AppCompatActivity() {
    companion object {
        val EXTRA_RECV_KIND = "RecvKind"
    }

    val vm = ConfigViewModel(10.0f, 1.0f, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_config)
        val binding: ActivityConfigBinding = DataBindingUtil.setContentView(this, R.layout.activity_config)
        binding.config = vm
    }

    fun onPushRemoteClick(view: View) {
        broadcastConfiguration(vm) { success, name ->  
            if(success) {
                Toast.makeText(this, "Pushed configuration to " + name, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onExecLocalClick(view: View) {
        createCameraActivity(this, LocalCommandSource(vm))
    }
}
