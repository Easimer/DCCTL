package net.easimer.dcctl.protocol

import java.io.InputStream

interface IClientSocket {
    val inputStream : InputStream
    fun close()
}