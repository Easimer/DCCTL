package net.easimer.dcctl

import android.graphics.Color
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
            val color = when(it.first) {
                LogLevel.Debug -> Color.DKGRAY
                LogLevel.Note -> Color.BLACK
                LogLevel.Warning -> Color.YELLOW
                LogLevel.Error -> Color.RED
            }
            textView.text = "[${it.first}] ${it.second}: ${it.third}"
            textView.setTextColor(color)
            logView.addView(textView)
        }
    }
}
