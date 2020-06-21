package net.easimer.dcctl.scripting.ui

import androidx.recyclerview.widget.RecyclerView
import net.easimer.dcctl.scripting.ScriptCommand

/**
 * Holder for the various CommandViews.
 */
class CommandViewHolder<T : ScriptCommand>(val view : CommandView<T>) : RecyclerView.ViewHolder(view)