package com.example.weatherapp.ui.settings.view_model

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.annotation.RequiresPermission
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.model.location.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel : ViewModel() {

    private val _notificationEnabled = MutableLiveData<Boolean>()
    private val _gpsLocationResult = MutableLiveData<Result<Pair<Float, Float>>>()

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("HomeSettingsPrefs", Context.MODE_PRIVATE)
    }

    fun getLocationPreference(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean("usingGPS", true)
    }

    fun getMapPreference(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean("usingMAP", false)
    }

    fun getNotificationPreference(context: Context): Boolean {
        val enabled = getSharedPreferences(context).getBoolean("notifications_enabled", false)
        _notificationEnabled.postValue(enabled)
        return enabled
    }

    fun saveLocationPreference(context: Context, useGps: Boolean, useMap: Boolean) {
        getSharedPreferences(context).edit {
            putBoolean("usingGPS", useGps)
            putBoolean("usingMAP", useMap)
        }
    }

    fun saveMapLocation(context: Context, latitude: Float, longitude: Float) {
        getSharedPreferences(context).edit {
            putFloat("home_latitude", latitude)
            putFloat("home_longitude", longitude)
            putBoolean("usingGPS", false)
            putBoolean("usingMAP", true)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun saveGpsLocation(context: Context, locationHelper: LocationHelper, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                val (lat, lon, _) = locationHelper.getLastKnownLocation()
                if (lat != null && lon != null) {
                    getSharedPreferences(context).edit {
                        putFloat("home_latitude", lat.toFloat())
                        putFloat("home_longitude", lon.toFloat())
                        putBoolean("usingGPS", true)
                        putBoolean("usingMAP", false)
                    }
                    _gpsLocationResult.postValue(Result.success(Pair(lat.toFloat(), lon.toFloat())))
                } else {
                    _gpsLocationResult.postValue(Result.failure(Exception("Unable to retrieve location")))
                }
            } catch (e: Exception) {
                _gpsLocationResult.postValue(Result.failure(e))
            }
        }
    }

    fun getGpsLocationResult(): MutableLiveData<Result<Pair<Float, Float>>> = _gpsLocationResult

    fun saveNotificationPreference(context: Context, enabled: Boolean) {
        getSharedPreferences(context).edit {
            putBoolean("notifications_enabled", enabled)
        }
        _notificationEnabled.postValue(enabled)
    }

    fun saveTemperatureUnit(context: Context, unit: String) {
        getSharedPreferences(context).edit {
            putString("temperature_unit", unit)
            // Enforce wind speed based on temperature unit
            if (unit == "imperial") {
                putString("wind_speed_unit", "mph")
            } else {
                putString("wind_speed_unit", "m/s")
            }
        }
    }

    fun getTemperatureUnit(context: Context): String {
        return getSharedPreferences(context).getString("temperature_unit", "standard") ?: "standard"
    }

    fun saveWindSpeedUnit(context: Context, unit: String) {
        getSharedPreferences(context).edit {
            putString("wind_speed_unit", unit)
        }
    }

    fun getWindSpeedUnit(context: Context): String {
        return getSharedPreferences(context).getString("wind_speed_unit", "m/s") ?: "m/s"
    }

    fun saveLanguage(context: Context, language: String) {
        getSharedPreferences(context).edit {
            putString("language", language)
        }
        updateLocale(context, language)
    }

    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLanguage(context: Context): String {
        return getSharedPreferences(context).getString("language", "en") ?: "en"
    }
}