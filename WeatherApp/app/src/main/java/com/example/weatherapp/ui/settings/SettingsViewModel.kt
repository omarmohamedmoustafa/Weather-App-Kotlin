package com.example.weatherapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private lateinit var prefs: SharedPreferences

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> = _temperature

    private val _windSpeed = MutableLiveData<String>()
    val windSpeed: LiveData<String> = _windSpeed

    private val _notifications = MutableLiveData<String>()
    val notifications: LiveData<String> = _notifications

    private val _latitude = MutableLiveData<Double?>()
    val latitude: LiveData<Double?> = _latitude

    private val _longitude = MutableLiveData<Double?>()
    val longitude: LiveData<Double?> = _longitude

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        loadSettings()
    }

    fun saveSettings(
        location: String,
        language: String,
        temperature: String,
        windSpeed: String,
        notifications: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            with(prefs.edit()) {
                putString("location", location)
                putString("language", language)
                putString("temperature", temperature)
                putString("wind_speed", windSpeed)
                putString("notifications", notifications)
                if (latitude != null) {
                    putFloat("home_latitude", latitude.toFloat())
                } else {
                    remove("home_latitude")
                }
                if (longitude != null) {
                    putFloat("home_longitude", longitude.toFloat())
                } else {
                    remove("home_longitude")
                }
                apply()
            }
            _location.value = location
            _language.value = language
            _temperature.value = temperature
            _windSpeed.value = windSpeed
            _notifications.value = notifications
            _latitude.value = latitude
            _longitude.value = longitude
        }
    }

    private fun loadSettings() {
        _location.value = prefs.getString("location", "GPS")
        _language.value = prefs.getString("language", "English")
        _temperature.value = prefs.getString("temperature", "Celsius")
        _windSpeed.value = prefs.getString("wind_speed", "Meter/Sec")
        _notifications.value = prefs.getString("notifications", "Disable")
        _latitude.value = if (prefs.contains("home_latitude")) prefs.getFloat("home_latitude", 0f).toDouble() else null
        _longitude.value = if (prefs.contains("home_longitude")) prefs.getFloat("home_longitude", 0f).toDouble() else null
    }
}