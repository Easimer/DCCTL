package net.easimer.dcctl.protocol

interface IServerSocket {
    fun accept() : IClientSocket?
    fun close()
}