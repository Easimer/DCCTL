package net.easimer.dcctl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import net.easimer.dcctl.databinding.ActivityConfigBinding

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_config)
        val binding: ActivityConfigBinding = DataBindingUtil.setContentView(this, R.layout.activity_config)
        binding.config = ConfigViewModel(10.0f, 1.0f, 0)
    }
}
