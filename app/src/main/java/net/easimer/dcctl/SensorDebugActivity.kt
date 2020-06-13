package net.easimer.dcctl

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import net.easimer.dcctl.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableFloat
import net.easimer.dcctl.databinding.ActivitySensorDebugBinding

class SensorDebugViewModel(
    var pressure: ObservableFloat,
    var altitude: ObservableFloat)

class SensorDebug : AppCompatActivity(), SensorEventListener {
    private val sd = SensorDebugViewModel(ObservableFloat(), ObservableFloat())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_sensor_debug)

        val binding: ActivitySensorDebugBinding = DataBindingUtil.setContentView(this, R.layout.activity_sensor_debug)
        binding.sd = sd


    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            event.apply {
                when (sensor.type) {
                    Sensor.TYPE_PRESSURE -> {
                        updatePressure(event.values[0])
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)

        Log.d("SensorDebug", "onResume")
    }

    override fun onPause() {
        super.onPause()

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    private fun updatePressure(pressure: Float) {
        sd.pressure.set(pressure)
        val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
        sd.altitude.set(altitude)
    }
}
