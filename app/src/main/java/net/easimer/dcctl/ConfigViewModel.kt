package net.easimer.dcctl

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter

class ConfigViewModel(
    var delay: Float,
    var interval: Float,
    var count: Int,
    var flash: Boolean
)

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


@BindingAdapter("android:text")
fun setInt(view: TextView, value: Int) {
    view.setText(value.toString())
}

@InverseBindingAdapter(attribute = "android:text")
fun getInt(view: TextView): Int {
    val sval = view.text.toString()
    if(sval.isEmpty()) {
        return 0
    } else {
        try {
            return sval.toInt()
        } catch(e: NumberFormatException) {
            return 0
        }
    }
}