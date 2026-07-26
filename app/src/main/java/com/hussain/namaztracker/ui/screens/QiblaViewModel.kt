package com.hussain.namaztracker.ui.screens

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hussain.namaztracker.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.*

data class QiblaState(
    val bearing: Float = 0f, // Angle to True North
    val qiblaDirection: Float = 0f, // Angle from True North to Qibla
    val distance: Float = 0f, // Distance in km
    val locationName: String = "Detecting location...",
    val hasLocation: Boolean = false,
    val isLocationCached: Boolean = false
)

class QiblaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(QiblaState())
    val uiState: StateFlow<QiblaState> = _uiState.asStateFlow()

    private val settingsManager = SettingsManager(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var declination = 0f

    private val meccaLat = 21.4225
    private val meccaLng = 39.8262

    init {
        loadCachedLocation()
        startLocationUpdates()
    }

    private fun loadCachedLocation() {
        viewModelScope.launch {
            settingsManager.lastLocation.first()?.let { (lat, lng) ->
                val cachedLocation = Location("cached").apply {
                    latitude = lat
                    longitude = lng
                }
                updateLocation(cachedLocation, isCached = true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        viewModelScope.launch {
            // 1. Try to get current high-accuracy location
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        updateLocation(location, isCached = false)
                        saveLocation(location)
                    } else {
                        // 2. Fallback to last known location if getCurrentLocation fails
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            lastLoc?.let {
                                updateLocation(it, isCached = false)
                                saveLocation(it)
                            }
                        }
                    }
                }
        }
    }

    private fun saveLocation(location: Location) {
        viewModelScope.launch {
            settingsManager.setLastLocation(location.latitude, location.longitude)
        }
    }

    private fun updateLocation(location: Location, isCached: Boolean) {
        val qiblaBearing = calculateQibla(location.latitude, location.longitude)
        
        val meccaLocation = Location("Mecca").apply {
            latitude = meccaLat
            longitude = meccaLng
        }
        val distance = location.distanceTo(meccaLocation) / 1000f

        // Update declination for True North correction
        val geomagneticField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = geomagneticField.declination

        _uiState.value = _uiState.value.copy(
            qiblaDirection = qiblaBearing.toFloat(),
            distance = distance,
            locationName = if (isCached) "Last Known Location" else "Current Location",
            hasLocation = true,
            isLocationCached = isCached
        )
    }

    private fun calculateQibla(lat: Double, lng: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(meccaLat)
        val lam1 = Math.toRadians(lng)
        val lam2 = Math.toRadians(meccaLng)

        val deltaLam = lam2 - lam1

        val y = sin(deltaLam) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLam)

        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        return bearing
    }

    fun startListening() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }

        if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthInRadians = orientation[0]
                var azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                
                // Correct for Magnetic Declination to get True North
                azimuthInDegrees += declination
                
                _uiState.value = _uiState.value.copy(bearing = azimuthInDegrees)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
