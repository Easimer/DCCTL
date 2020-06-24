package net.easimer.dcctl.scripting.ui

import androidx.recyclerview.widget.RecyclerView
import net.easimer.dcctl.scripting.Command

/**
 * Holder for the various CommandViews.
 */
class CommandViewHolder<T : Command>(val view : CommandView<T>) : RecyclerView.ViewHolder(view)