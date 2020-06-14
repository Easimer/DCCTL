package net.easimer.dcctl.scripting

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.easimer.dcctl.Log
import net.easimer.dcctl.R
import net.easimer.dcctl.databinding.AudioSignalCommandViewBinding
import net.easimer.dcctl.databinding.CaptureMultipleCommandViewBinding
import net.easimer.dcctl.databinding.WaitCommandViewBinding
import net.easimer.dcctl.protocol.broadcastScript
import java.util.*

class ScriptActivity : AppCompatActivity() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewAdapter: CommandAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val script = LinkedList<ScriptCommand>()
    private val deleter = CommandDeleter(script) { cmd ->
        val idx = script.indexOfRef(cmd)
        script.removeAt(idx)
        viewAdapter.notifyItemRemoved(idx)
        Log.d("ScriptActivity", "Removed item $idx")
    }

    private fun <T> List<T>.indexOfRef(elem : T) : Int {
        var ret = -1
        forEachIndexed { i, it ->
            if(elem === it) {
                ret = i
            }
        }

        return ret
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script)

        viewManager = LinearLayoutManager(this)
        viewAdapter = CommandAdapter(this, deleter)

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

        val cmd = when (item.itemId) {
            R.id.new_wait ->
                ScriptCommand.Wait(5.0f)
            R.id.new_capture_multiple ->
                ScriptCommand.CaptureMultiple(1.0f, 3)
            R.id.new_audio_signal ->
                ScriptCommand.AudioSignal(SoundEffect.Blip)
            else ->
                null
        }

        if(cmd != null) {
            val idx = script.size
            script.add(cmd)
            viewAdapter.submitList(script)
            viewAdapter.notifyDataSetChanged()
            viewAdapter.notifyItemInserted(idx)
            return true
        } else {
            if(item.itemId == R.id.send) {
                val script = Script(script)

                broadcastScript(script) { success, name ->
                    if (success) {
                        val toast = Toast.makeText(
                            this,
                            "Pushed configuration to $name",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                    }
                }
                return true
            } else {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    interface ICommandView {
        fun bindTo(cmd: ScriptCommand)
    }

    sealed class CommandView<T : ScriptCommand>(ctx: Context, private val deleteCallback: (ScriptCommand) -> Unit) : ICommandView, LinearLayout(ctx) {
        var onClickDelete : (View) -> Unit = {}
        protected abstract var cmd : T

        protected val inflater =
            ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun addView(child : View) {
            child.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            super.addView(child)
        }

        class Wait(ctx: Context, deleteCallback: (ScriptCommand) -> Unit)
            : CommandView<ScriptCommand.Wait>(ctx, deleteCallback) {
            private val binding
                    = WaitCommandViewBinding.inflate(inflater)

            override var cmd = ScriptCommand.Wait(0.0f)

            override fun bindTo(cmd: ScriptCommand) {
                if(cmd is ScriptCommand.Wait) {
                    this.cmd = cmd
                    binding.cmd = cmd
                }
            }

            init {
                addView(binding.root)
                attachDeleter()
            }
        }

        class CaptureMultiple(ctx: Context, deleteCallback: (ScriptCommand) -> Unit)
            : CommandView<ScriptCommand.CaptureMultiple>(ctx, deleteCallback) {
            private val binding
                    = CaptureMultipleCommandViewBinding.inflate(inflater)
            override var cmd = ScriptCommand.CaptureMultiple(0.0f, 0)

            override fun bindTo(cmd: ScriptCommand) {
                if(cmd is ScriptCommand.CaptureMultiple) {
                    this.cmd = cmd
                    binding.cmd = cmd
                }
            }

            init {
                addView(binding.root)
                attachDeleter()
            }
        }

        class AudioSignal(ctx: Context, deleteCallback: (ScriptCommand) -> Unit)
            : CommandView<ScriptCommand.AudioSignal>(ctx, deleteCallback), AdapterView.OnItemSelectedListener {
            private val binding
                    = AudioSignalCommandViewBinding.inflate(inflater)

            override var cmd = ScriptCommand.AudioSignal(SoundEffect.Blip)

            override fun bindTo(cmd: ScriptCommand) {
                if(cmd is ScriptCommand.AudioSignal) {
                    this.cmd = cmd
                    binding.cmd = cmd
                }
            }

            init {
                addView(binding.root)
                val spinner = binding.root.findViewById<Spinner>(R.id.spinner)
                spinner.adapter = ArrayAdapter<SoundEffect>(ctx, R.layout.support_simple_spinner_dropdown_item, SoundEffect.values())
                spinner.onItemSelectedListener = this
                attachDeleter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                cmd.id = SoundEffect.Blip
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.also {
                    val item = parent.getItemAtPosition(position)
                    if(item is SoundEffect) {
                        cmd.id = item
                    }
                }
            }
        }


        fun attachDeleter() {
            val deleteButton = findViewById<View>(R.id.deleteButton)
            deleteButton?.setOnClickListener {
                deleteCallback(cmd)
            }
        }
    }

    private class CommandViewHolder<T : ScriptCommand>(val view : CommandView<T>) : RecyclerView.ViewHolder(view)

    private class CommandDiffCallback : DiffUtil.ItemCallback<ScriptCommand>() {
        override fun areItemsTheSame(oldItem: ScriptCommand, newItem: ScriptCommand): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ScriptCommand, newItem: ScriptCommand): Boolean {
            return oldItem == newItem
        }
    }

    private class CommandDeleter(
        private val script : List<ScriptCommand>,
        val callback : (ScriptCommand) -> Unit
    )

    private class CommandAdapter(private val ctx : Context, private val deleter : CommandDeleter) : ListAdapter<ScriptCommand, RecyclerView.ViewHolder>(CommandDiffCallback()) {
        private fun setLayout(cv : LinearLayout) {
            cv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = when(viewType) {
                CMD_WAIT -> CommandView.Wait(ctx, deleter.callback)
                CMD_CAPTURE_MULTIPLE -> CommandView.CaptureMultiple(ctx, deleter.callback)
                CMD_AUDIO_SIGNAL -> CommandView.AudioSignal(ctx, deleter.callback)
                else -> CommandView.Wait(ctx, deleter.callback)
            }
            setLayout(v)
            return CommandViewHolder(v)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder.itemView as ICommandView).bindTo(getItem(position))
        }

        override fun getItemViewType(position: Int): Int {
            return when(getItem(position)) {
                is ScriptCommand.Wait -> CMD_WAIT
                is ScriptCommand.CaptureMultiple -> CMD_CAPTURE_MULTIPLE
                is ScriptCommand.AudioSignal -> CMD_AUDIO_SIGNAL
            }
        }

        companion object {
            private const val CMD_WAIT = 0
            private const val CMD_CAPTURE_MULTIPLE = 1
            private const val CMD_AUDIO_SIGNAL = 2
        }

    }
}
