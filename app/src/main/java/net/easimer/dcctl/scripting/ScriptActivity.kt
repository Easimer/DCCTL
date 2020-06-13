package net.easimer.dcctl.scripting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import net.easimer.dcctl.R

class ScriptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_script, menu)
        return true
    }
}
