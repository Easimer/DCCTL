package net.easimer.dcctl

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.locks.Lock

enum class LogLevel {
    Debug,
    Note,
    Warning,
    Error,
}

interface ILogger {
    fun d(tag: String, msg: String, level : LogLevel = LogLevel.Note)
    val messages : Collection<Triple<LogLevel, String, String>>
}

object Log : ILogger {
    override fun d(tag: String, msg: String, level: LogLevel) {
        synchronized(this) {
            messages.add(Triple(level, tag, msg))
            android.util.Log.d(tag, msg)
        }
    }

    override var messages = LinkedList<Triple<LogLevel, String, String>>()
}