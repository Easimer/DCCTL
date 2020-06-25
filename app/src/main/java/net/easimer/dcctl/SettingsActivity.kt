package net.easimer.dcctl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import net.easimer.dcctl.protocol.forEachPairedBluetoothDevice
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val context = preferenceManager.context
            val screen = preferenceManager.createPreferenceScreen(context)
            val categoryNet = PreferenceCategory(context)
            categoryNet.title = getString(R.string.network)
            categoryNet.key = "networking"
            screen.addPreference(categoryNet)

            val categoryAdv = PreferenceCategory(context)
            categoryAdv.title = getString(R.string.advanced)
            categoryAdv.key = "advanced"
            screen.addPreference(categoryAdv)

            MultiSelectListPreference(context).run {
                title = getString(R.string.pref_excluded_devices)
                key = "excluded_devices"

                val entryList = LinkedList<String>()
                val entryValueList = LinkedList<String>()
                forEachPairedBluetoothDevice { name, id ->
                    entryList.add(name)
                    entryValueList.add(id)
                }

                entries = entryList.toTypedArray()
                entryValues = entryValueList.toTypedArray()

                categoryNet.addPreference(this)
            }

            CheckBoxPreference(context).run {
                title = getString(R.string.pref_tcp_server)
                key = "tcp_server_enabled"
                categoryAdv.addPreference(this)
            }

            preferenceScreen = screen
        }
    }
}