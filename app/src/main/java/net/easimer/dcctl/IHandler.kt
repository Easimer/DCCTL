package net.easimer.dcctl

interface IHandler {
    fun post(r: () -> Unit): Boolean
}