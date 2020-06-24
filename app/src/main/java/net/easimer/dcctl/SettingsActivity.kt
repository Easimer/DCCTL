package net.easimer.dcctl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

            val preferenceBroadcast = MultiSelectListPreference(context)
            preferenceBroadcast.title = getString(R.string.pref_excluded_devices)
            preferenceBroadcast.key = "excluded_devices"

            val entries = LinkedList<String>()
            val entryValues = LinkedList<String>()
            forEachPairedBluetoothDevice { name, id ->
                entries.add(name)
                entryValues.add(id)
            }

            preferenceBroadcast.entries = entries.toTypedArray()
            preferenceBroadcast.entryValues = entryValues.toTypedArray()

            categoryNet.addPreference(preferenceBroadcast)

            preferenceScreen = screen
        }
    }
}