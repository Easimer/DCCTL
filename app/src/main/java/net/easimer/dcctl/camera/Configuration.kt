package net.easimer.dcctl.camera

import android.content.Context
import androidx.preference.PreferenceManager

data class Configuration(private val ctx: Context) {
    private val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
    val tcpServerEnabled = pref.getBoolean("tcp_server_enabled", false)
}