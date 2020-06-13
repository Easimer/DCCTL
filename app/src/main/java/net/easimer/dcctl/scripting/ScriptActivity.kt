package net.easimer.dcctl.scripting

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.easimer.dcctl.R
import net.easimer.dcctl.databinding.ActivityConfigBinding
import net.easimer.dcctl.databinding.ActivityConfigBinding.inflate
import net.easimer.dcctl.databinding.AudioSignalCommandViewBinding
import net.easimer.dcctl.databinding.CaptureMultipleCommandViewBinding
import net.easimer.dcctl.databinding.WaitCommandViewBinding
import java.util.*

class ScriptActivity : AppCompatActivity() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val script = LinkedList<ScriptCommand>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ScriptAdapter(script)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_script, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            // R.id.new_wait ->
            else -> super.onOptionsItemSelected(item)
        }
    }

    interface ICommandView {
        val cmd : ScriptCommand
    }

    abstract class CommandView(ctx: Context) : ICommandView, LinearLayout(ctx) {
        var onClickDelete : (View) -> Unit = {}

        protected val inflater =
            ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }

    private class WaitCommandView(ctx: Context, override val cmd : ScriptCommand.Wait)
        : CommandView(ctx) {
        private val binding
                = WaitCommandViewBinding.inflate(inflater)
    }

    private class CaptureMultipleCommandView(ctx: Context, override val cmd : ScriptCommand.CaptureMultiple)
        : CommandView(ctx) {
        private val binding
                = CaptureMultipleCommandViewBinding.inflate(inflater)
    }

    private class AudioSignalCommandView(ctx: Context, override val cmd : ScriptCommand.AudioSignal)
        : CommandView(ctx) {
        private val binding
                = AudioSignalCommandViewBinding.inflate(inflater)
    }

    private class ScriptAdapter(private val script : List<ScriptCommand>) : RecyclerView.Adapter<ScriptAdapter.ViewHolder>() {
        private class ViewHolder(val container : LinearLayout) : RecyclerView.ViewHolder(container)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val ll = LinearLayout(parent.context)
            return ViewHolder(ll)
        }

        override fun getItemCount(): Int = script.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cmd = script[position]
            holder.container.addView(commandViewFactory(holder.container.context, cmd))
        }

        private fun commandViewFactory(ctx: Context, cmd : ScriptCommand): View {
            return when(cmd) {
                is ScriptCommand.Wait -> WaitCommandView(ctx, cmd)
                is ScriptCommand.CaptureMultiple -> CaptureMultipleCommandView(ctx, cmd)
                is ScriptCommand.AudioSignal -> AudioSignalCommandView(ctx, cmd)
            }
        }
    }
}
