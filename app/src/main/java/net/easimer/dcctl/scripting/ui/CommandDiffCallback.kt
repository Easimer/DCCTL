package net.easimer.dcctl.scripting.ui

import androidx.recyclerview.widget.DiffUtil
import net.easimer.dcctl.scripting.ScriptCommand

/**
 * Diffing callback used by the RecyclerView to determine whether the values in the list have
 * changed.
 */
class CommandDiffCallback : DiffUtil.ItemCallback<ScriptCommand>() {
    override fun areItemsTheSame(oldItem: ScriptCommand, newItem: ScriptCommand): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: ScriptCommand, newItem: ScriptCommand): Boolean {
        return oldItem == newItem
    }
}