package net.easimer.dcctl.protocol

import android.content.Context

const val SERVER_KIND_BLUETOOTH = 1
const val SERVER_KIND_TCP = 2

fun createServerSocket(ctx: Context, kind : Int): IServerSocket? {
    return when(kind) {
        SERVER_KIND_BLUETOOTH -> createBluetoothServerSocket(ctx)
        SERVER_KIND_TCP -> createTCPServerSocket(ctx)
        else -> null
    }
}