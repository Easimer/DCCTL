package net.easimer.dcctl.scripting.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import net.easimer.dcctl.R
import net.easimer.dcctl.databinding.AudioSignalCommandViewBinding
import net.easimer.dcctl.databinding.BlinkCommandViewBinding
import net.easimer.dcctl.databinding.CaptureMultipleCommandViewBinding
import net.easimer.dcctl.databinding.WaitCommandViewBinding
import net.easimer.dcctl.scripting.ScriptCommand
import net.easimer.dcctl.scripting.SoundEffect

/**
 * A user interface element that represents a single script command.
 * @param ctx Context
 * @param deleteCallback Called when the view wants to delete itself (the user clicked on the
 * delete button)
 */
sealed class CommandView<T : ScriptCommand>(ctx: Context, private val deleteCallback: (ScriptCommand) -> Unit) : ICommandView, LinearLayout(ctx) {
    protected abstract var cmd : T

    protected val inflater =
        ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun addView(child : View) {
        child.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        super.addView(child)
    }

    /**
     * If the view has a delete button, then set it's onClickListener.
     */
    protected fun attachDeleter() {
        val deleteButton = findViewById<View>(R.id.deleteButton)
        deleteButton?.setOnClickListener {
            deleteCallback(cmd)
        }
    }

    class Wait(ctx: Context, deleteCallback: (ScriptCommand) -> Unit)
        : CommandView<ScriptCommand.Wait>(ctx, deleteCallback) {
        private val binding =
            WaitCommandViewBinding.inflate(inflater)

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
        private val binding =
            CaptureMultipleCommandViewBinding.inflate(inflater)

        override var cmd = ScriptCommand.CaptureMultiple(0.0f, 0, false)

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
        : CommandView<ScriptCommand.AudioSignal>(ctx, deleteCallback),
        AdapterView.OnItemSelectedListener {
        private val binding
                =
            AudioSignalCommandViewBinding.inflate(
                inflater
            )

        override var cmd =
            ScriptCommand.AudioSignal(
                SoundEffect.Blip
            )

        override fun bindTo(cmd: ScriptCommand) {
            if(cmd is ScriptCommand.AudioSignal) {
                this.cmd = cmd
                binding.cmd = cmd
            }
        }

        init {
            val spinner = binding.root.findViewById<Spinner>(
                R.id.spinner
            )
            spinner.adapter =
                ArrayAdapter<SoundEffect>(
                    ctx,
                    R.layout.support_simple_spinner_dropdown_item,
                    SoundEffect.values()
                )
            spinner.onItemSelectedListener = this
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

        init {
            addView(binding.root)
            attachDeleter()
        }
    }

    class Blink(ctx: Context, deleteCallback: (ScriptCommand) -> Unit)
        : CommandView<ScriptCommand.Blink>(ctx, deleteCallback) {
        private val binding = BlinkCommandViewBinding.inflate(inflater)
        override var cmd = ScriptCommand.Blink(0.0f)

        override fun bindTo(cmd: ScriptCommand) {
            if(cmd is ScriptCommand.Blink) {
                this.cmd = cmd
                binding.cmd = cmd
            }
        }

        init {
            addView(binding.root)
            attachDeleter()
        }
    }
}