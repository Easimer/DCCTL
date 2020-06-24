package net.easimer.dcctl.utils

import java.util.*

class Event<TCallback> {
    private val observers : MutableList<(TCallback) -> Unit>
            = LinkedList<(TCallback) -> Unit>()

    @Synchronized
    operator fun invoke(ev : TCallback) {
        observers.forEach {
            it(ev)
        }
    }

    @Synchronized
    operator fun plusAssign(observer : (TCallback) -> Unit) {
        observers.add(observer)
    }

    @Synchronized
    operator fun minusAssign(observer : (TCallback) -> Unit) {
        observers.remove(observer)
    }
}