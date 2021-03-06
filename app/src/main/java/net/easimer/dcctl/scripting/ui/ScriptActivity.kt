package net.easimer.dcctl.scripting.ui

import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.easimer.dcctl.ActivityResultCallbackActivity
import net.easimer.dcctl.Log
import net.easimer.dcctl.R
import net.easimer.dcctl.protocol.broadcastScript
import net.easimer.dcctl.scripting.Script
import net.easimer.dcctl.scripting.Command
import net.easimer.dcctl.scripting.SoundEffect
import java.util.*

class ScriptActivity : ActivityResultCallbackActivity() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewAdapter: CommandAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val script = LinkedList<Command>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script)

        val deleter = CommandDeleter(script) { cmd ->
            val idx = script.indexOfRef(cmd)
            if (idx >= 0) {
                script.removeAt(idx)
                viewAdapter.notifyItemRemoved(idx)
                Log.d("ScriptActivity", "Removed item $idx")
            }
        }

        // Create the RecyclerView
        viewManager = LinearLayoutManager(this)
        viewAdapter =
            CommandAdapter(
                this,
                deleter
            )

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = viewManager
            adapter = viewAdapter

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_script, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection

        val cmd = when (item.itemId) {
            R.id.new_wait ->
                Command.Wait(5.0f)
            R.id.new_capture_multiple ->
                Command.CaptureMultiple(
                    1.0f,
                    3,
                    false
                )
            R.id.new_audio_signal ->
                Command.AudioSignal(
                    SoundEffect.Blip
                )
            R.id.new_blink ->
                Command.Blink(0.5f)
            else ->
                null
        }

        if(cmd != null) {
            // If the selected menu item made a new script command
            // we place it in the list and notify the RecyclerList.
            val idx = script.size
            script.add(cmd)
            viewAdapter.submitList(script)
            viewAdapter.notifyDataSetChanged()
            viewAdapter.notifyItemInserted(idx)
            return true
        } else {
            // If the selected menu item didn't make a new script command
            // then the user wants to do something special, like to send
            // the script.
            if(item.itemId == R.id.send) {
                checkBluetoothAndSendScript()
                return true
            } else {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * A indexOf implementation for List<T> containers that
     * checks for equality not by value but by reference.
     *
     * @param elem The element to look for
     * @return The index of the object in the list or -1 if the list doesn't contain this object.
     */
    private fun <T> List<T>.indexOfRef(elem : T) : Int {
        forEachIndexed { i, it ->
            if(elem === it) {
                return i
            }
        }
        return -1
    }

    /**
     * Broadcast the script that the user built.
     */
    private fun checkBluetoothAndSendScript(dialog: Boolean = false) {
        BluetoothAdapter.getDefaultAdapter()?.let {
            if(it.isEnabled) {
                sendScript()
            } else {
                if(dialog) {
                    // Tell the user why we need Bluetooth to be turned on
                    AlertDialog.Builder(this)
                        .setTitle(R.string.bluetooth_dialog_title)
                        .setMessage(R.string.bluetooth_dialog_message)
                        .setPositiveButton(R.string.bluetooth_dialog_retry) { _, _ ->
                            // User clicked "retry", show the BT dialog again
                            startActivityForResult(BluetoothAdapter.ACTION_REQUEST_ENABLE) {
                                checkBluetoothAndSendScript(true)
                            }
                        }
                        .setNegativeButton(R.string.bluetooth_dialog_ok) { _, _ ->
                            // User acknowledged the fact that we can't function without bluetooth
                        }
                        .show()
                } else {
                    startActivityForResult(BluetoothAdapter.ACTION_REQUEST_ENABLE) {
                        checkBluetoothAndSendScript(true)
                    }
                }
            }
        }
    }

    private fun sendScript() {
        val script = Script(script)

        val excludedDevicesDefault = HashSet<String>()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val excludedDevicesPref = pref.getStringSet("excluded_devices", excludedDevicesDefault)
        val excludedDevices =
            if (excludedDevicesPref != null) excludedDevicesPref else excludedDevicesDefault

        broadcastScript(script, { success, name ->
            if (success) {
                runOnUiThread {
                    val toast = Toast.makeText(
                        this,
                        "Pushed configuration to $name",
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                }
            }
        }, { id -> !excludedDevices.contains(id) })
    }
}
