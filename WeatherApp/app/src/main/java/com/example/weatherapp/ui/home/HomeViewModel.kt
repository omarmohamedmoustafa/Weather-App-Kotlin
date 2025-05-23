package com.example.weatherapp.ui.home

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.location.LocationHelper
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.repository.WeatherRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {
    private val _currentWeather = MutableLiveData<CurrentWeatherResponse>()
    val currentWeather: LiveData<CurrentWeatherResponse> get() = _currentWeather

    private val _forecastWeather = MutableLiveData<WeatherResponse>()
    val forecastWeather: LiveData<WeatherResponse> get() = _forecastWeather

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchWeatherDataWithLocation(
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ) {
        if (!locationHelper.hasLocationPermissions()) {
            _errorMessage.value = "Location permissions not granted"
            return
        }

        _isLoading.value = true
        locationHelper.startLocationUpdates()
        locationHelper.onLocationUpdate = { longitude, latitude, _ ->
            if (latitude != null && longitude != null) {
                fetchWeatherData(latitude.toFloat(), longitude.toFloat(), apiKey, units, language)
            } else {
                _errorMessage.value = "Unable to retrieve location"
                _isLoading.value = false
            }
        }
    }

    private fun fetchWeatherData(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String,
        language: String
    ) {
        viewModelScope.launch {
            // Fetch current weather
            Log.d("HomeViewModel", "Fetching current weather for lat: $latitude, lon: $longitude")
            val currentResult = repository.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )

            currentResult.onSuccess { currentWeatherResponse ->
                Log.d("HomeViewModel", "Current weather fetched: $currentWeatherResponse")
                _currentWeather.value = currentWeatherResponse
                _errorMessage.value = null
            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error fetching current weather: ${exception.message}", exception)
                _errorMessage.value = exception.message ?: "Failed to fetch current weather data"
            }

            // Fetch 96-hour forecast
            Log.d("HomeViewModel", "Fetching forecast for lat: $latitude, lon: $longitude")
            val forecastResult = repository.getWeatherForecast(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )

            forecastResult.onSuccess { forecastResponse ->
                Log.d("HomeViewModel", "Forecast data fetched: $forecastResponse")
                _forecastWeather.value = forecastResponse
                _errorMessage.value = null
            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error fetching forecast: ${exception.message}", exception)
                _errorMessage.value = exception.message ?: "Failed to fetch forecast data"
            }

            _isLoading.value = false
        }
    }

    fun formatDateTime(timestamp: Long, timezone: Long): Pair<String, String> {
        val offsetMillis = timezone * 1000

        // Create a TimeZone object with the given offset
        val timeZone = TimeZone.getTimeZone("GMT")
        timeZone.rawOffset = offsetMillis.toInt()

        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        dateFormat.timeZone = timeZone
        timeFormat.timeZone = timeZone

        val date = Date(timestamp * 1000)

        return Pair(
            dateFormat.format(date),
            timeFormat.format(date)
        )
    }

    fun formatHourlyTime(timestamp: Long, timezone: Long): String {
        val timeFormat = SimpleDateFormat("h a", Locale.getDefault())
        // Create a TimeZone object with the given offset
        val timeZone = TimeZone.getTimeZone("GMT")
        timeZone.rawOffset = (timezone * 1000).toInt()
        timeFormat.timeZone = timeZone
        val date = Date(timestamp * 1000)
        return timeFormat.format(date)
    }

    fun formatUnixTimeWithOffset(unixTimestamp: Long, timezoneOffsetSeconds: Int): String {
        val date = Date(unixTimestamp * 1000)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Create TimeZone from offset
        val offsetMillis = timezoneOffsetSeconds * 1000
        val timeZone = object : TimeZone() {
            override fun getOffset(
                era: Int, year: Int, month: Int, day: Int, dayOfWeek: Int, milliseconds: Int
            ) = offsetMillis

            override fun setRawOffset(offsetMillis: Int) {
            }

            override fun getRawOffset() = offsetMillis
            override fun useDaylightTime() = false
            override fun inDaylightTime(date: Date?) = false
        }

        format.timeZone = timeZone
        return format.format(date)
    }

    override fun onCleared() {
        super.onCleared()
        locationHelper.stopLocationUpdates()
    }
}