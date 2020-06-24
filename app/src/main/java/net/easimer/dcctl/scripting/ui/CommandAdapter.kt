package net.easimer.dcctl.scripting.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.easimer.dcctl.scripting.Command

/**
 * List adapter for RecyclerView.
 */
class CommandAdapter(private val ctx : Context, private val deleter : CommandDeleter) : ListAdapter<Command, RecyclerView.ViewHolder>(
    CommandDiffCallback()
) {
    private fun setLayout(cv : LinearLayout) {
        cv.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = when(viewType) {
            CMD_WAIT -> CommandView.Wait(
                ctx,
                deleter.callback
            )
            CMD_CAPTURE_MULTIPLE -> CommandView.CaptureMultiple(
                ctx,
                deleter.callback
            )
            CMD_AUDIO_SIGNAL -> CommandView.AudioSignal(
                ctx,
                deleter.callback
            )
            CMD_BLINK -> CommandView.Blink(
                ctx,
                deleter.callback
            )
            else -> CommandView.Wait(
                ctx,
                deleter.callback
            )
        }
        setLayout(v)
        return CommandViewHolder(
            v
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as ICommandView).bindTo(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is Command.Wait -> CMD_WAIT
            is Command.CaptureMultiple -> CMD_CAPTURE_MULTIPLE
            is Command.AudioSignal -> CMD_AUDIO_SIGNAL
            is Command.Blink -> CMD_BLINK
        }
    }

    companion object {
        private const val CMD_WAIT = 0
        private const val CMD_CAPTURE_MULTIPLE = 1
        private const val CMD_AUDIO_SIGNAL = 2
        private const val CMD_BLINK = 3
    }
}