package net.easimer.dcctl

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import java.lang.NumberFormatException

data class ConfigViewModel(
    override val delay: Float,
    override val interval: Float,
    override val count: Int
) : IConfigSource {
    // ViewModel is always available as a config source
    override val isReady: Boolean
        get() = true
}

// Binding helpers

@BindingAdapter("android:text")
fun setFloat(view: TextView, value: Float) {
    if(value.isNaN()) {
        view.setText("")
    } else {
        view.setText(value.toString())
    }
}

@InverseBindingAdapter(attribute = "android:text")
fun getFloat(view: TextView): Float {
    val sval = view.text.toString()
    if(sval.isEmpty()) {
        return 0.0f
    } else {
        try {
            return sval.toFloat()
        } catch(e: NumberFormatException) {
            return 0.0f
        }
    }
}