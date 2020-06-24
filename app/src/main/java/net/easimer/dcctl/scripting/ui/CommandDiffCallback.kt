package net.easimer.dcctl.scripting.ui

import androidx.recyclerview.widget.DiffUtil
import net.easimer.dcctl.scripting.Command

/**
 * Diffing callback used by the RecyclerView to determine whether the values in the list have
 * changed.
 */
class CommandDiffCallback : DiffUtil.ItemCallback<Command>() {
    override fun areItemsTheSame(oldItem: Command, newItem: Command): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Command, newItem: Command): Boolean {
        return oldItem == newItem
    }
}