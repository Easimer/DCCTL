package net.easimer.dcctl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class LogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val logView = findViewById<LinearLayout>(R.id.log_view)
        addMessages(logView, Log.messages)
    }

    private fun addMessages(logView: LinearLayout, messages : Collection<Triple<LogLevel, String, String>>) {
        messages.forEach {
            val textView = TextView(this)
            textView.text = "[${it.first}] ${it.second}: ${it.third}"
            logView.addView(textView)
        }
    }
}
