package net.easimer.dcctl.protocol

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import net.easimer.dcctl.Log
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

private class TcpClientSocketImpl(private val sock : Socket) : IClientSocket {
    override val inputStream: InputStream
        get() = sock.inputStream

    override fun close() {
        sock.close()
    }
}

private class TcpServerSocketImpl(
    private val sock: ServerSocket,
    private val cleanupFunc: () -> Unit
) : IServerSocket {
    override fun accept(): IClientSocket? {
        val csock = sock.accept()
        if(csock != null) {
            return TcpClientSocketImpl(csock)
        }

        return null
    }

    override fun close() {
        cleanupFunc()
        sock.close()
    }
}

fun createTCPServerSocket(ctx: Context): IServerSocket? {
    val sock = ServerSocket(0).also { sock ->

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "DCCTL Command Server"
            serviceType = "_dcctlcmd._tcp"
            setPort(sock.localPort)
        }

        val registrationListener = object : NsdManager.RegistrationListener {
            private val TAG = "NsdRegMan"

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                serviceInfo?.let {
                    Log.d(TAG, "Service ${it.serviceName} (${it.serviceType}) registered on port ${it.port}")
                }
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                serviceInfo?.let {
                    Log.d(TAG, "Service ${it.serviceName} (${it.serviceType}) unregistered")
                }
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                serviceInfo?.let {
                    Log.d(TAG,
                        "Service ${it.serviceName} (${it.serviceType}) couldn't be registered, err=$errorCode")
                }
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                serviceInfo?.let {
                    Log.d(TAG,
                        "Service ${it.serviceName} (${it.serviceType}) couldn't be unregistered, err=$errorCode")
                }
            }
        }

        val nsdManager = (ctx.getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }

        return TcpServerSocketImpl(sock) {
            // Unregister our service on shutdown
            nsdManager.unregisterService(registrationListener)
        }
    }
    return null
}