package net.easimer.dcctl

import android.content.Context
import android.content.Intent
import android.os.Build
import net.easimer.dcctl.camera.CameraService
import java.util.*

object CameraServiceManager {
    private val TAG = "CameraServiceManager"
    private var serviceIntent: Intent? = null
    private val serviceEventListeners : MutableList<IServiceManagementEventListener>
            = LinkedList<IServiceManagementEventListener>()

    /**
     * Start the camera service if it's not running yet
     * @param ctx The context
     */
    @Synchronized
    fun startIfDoesntExist(ctx: Context): Boolean {
        if(serviceIntent == null) {
            Log.d(TAG, "Creating the camera service")
            Intent(ctx, CameraService::class.java).also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(it)
                } else {
                    ctx.startService(it)
                }
                serviceIntent = it
            }

            serviceEventListeners.forEach {
                it.onServiceStarted()
            }
            return true
        }
        return false
    }

    /**
     * Stop the camera service if it's running at the moment.
     * @param ctx The context
     */
    @Synchronized
    fun stopIfRunning(ctx: Context): Boolean {
        if(serviceIntent != null) {
            Log.d(TAG, "Stopping the camera service")
            ctx.stopService(serviceIntent)
            serviceIntent = null

            serviceEventListeners.forEach {
                it.onServiceStopped()
            }
            return true
        }
        return false
    }

    interface IServiceManagementEventListener {
        /**
         * Called when the service has been started (or is already running if the updateImmediately
         * parameter is true when subscribing)
         */
        fun onServiceStarted()

        /**
         * Called when the service has been stopped (or is already stopped if the updateImmediately
         * parameter is true when subscribing)
         */
        fun onServiceStopped()
    }

    /**
     * Subscribe to service management events.
     * @param listener the observer
     * @param updateImmediately should the observer be notified about the current status of the
     * service
     */
    @Synchronized
    fun addServiceEventListener(listener : IServiceManagementEventListener, updateImmediately : Boolean) {
        serviceEventListeners.add(listener)

        if(updateImmediately) {
            if(serviceIntent != null) {
                listener.onServiceStarted()
            } else {
                listener.onServiceStopped()
            }
        }
    }

    /**
     * Unsubscribe from service management events
     * @param listener the observer
     */
    @Synchronized
    fun removeServiceEventListener(listener : IServiceManagementEventListener) {
        serviceEventListeners.remove(listener)
    }
}