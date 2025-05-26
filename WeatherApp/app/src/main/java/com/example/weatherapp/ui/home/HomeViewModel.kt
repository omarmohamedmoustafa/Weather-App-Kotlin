package com.example.weatherapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.key._apiKey
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.repository.WeatherAppRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: WeatherAppRepository,
    private val latitude: Float? = null,
    private val longitude: Float? = null
) : ViewModel() {
    private val _currentWeather = MutableLiveData<CurrentWeatherResponse>()
    val currentWeather: LiveData<CurrentWeatherResponse> = _currentWeather

    private val _forecastWeather = MutableLiveData<WeatherResponse>()
    val forecastWeather: LiveData<WeatherResponse> = _forecastWeather

//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        if (latitude != null && longitude != null) {
            fetchWeatherData(latitude, longitude, _apiKey)
        }
    }
    fun fetchWeatherData(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ) {
//        _isLoading.value = true
        viewModelScope.launch {
            Log.d("HomeViewModel", "Fetching weather data for lat=$latitude, lon=$longitude")
            try {
                val currentWeatherResult = repository.getCurrentWeather(latitude, longitude, apiKey, units, language)
                val forecastWeatherResult = repository.getWeatherForecast(latitude, longitude, apiKey, units, language)

                currentWeatherResult.onSuccess { currentWeatherResponse ->
                    _currentWeather.value = currentWeatherResponse
                }.onFailure { exception ->
                    Log.e("HomeViewModel", "Error fetching current weather: ${exception.message}", exception)
                }

                forecastWeatherResult.onSuccess { forecastResponse ->
                    _forecastWeather.value = forecastResponse
                }.onFailure { exception ->
                    Log.e("HomeViewModel", "Error fetching forecast weather: ${exception.message}", exception)
                }

                if (currentWeatherResult.isFailure && forecastWeatherResult.isFailure) {
                    _errorMessage.value = "Failed to fetch weather data"
                } else {
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Unexpected error: ${e.message}", e)
                _errorMessage.value = e.message ?: "Unexpected error occurred"
            } finally {
//                _isLoading.value = false
            }
        }
    }

    fun formatDateTime(timestamp: Long, timezone: Long): Pair<String, String> {
        val offsetMillis = timezone * 1000
        val timeZone = TimeZone.getTimeZone("GMT")
        timeZone.rawOffset = offsetMillis.toInt()

        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        dateFormat.timeZone = timeZone
        timeFormat.timeZone = timeZone

        val date = Date(timestamp * 1000)
        return Pair(dateFormat.format(date), timeFormat.format(date))
    }

    fun formatHourlyTime(timestamp: Long, timezone: Long): String {
        val timeFormat = SimpleDateFormat("h a", Locale.getDefault())
        val timeZone = TimeZone.getTimeZone("GMT")
        timeZone.rawOffset = (timezone * 1000).toInt()
        timeFormat.timeZone = timeZone
        val date = Date(timestamp * 1000)
        return timeFormat.format(date)
    }

    fun formatUnixTimeWithOffset(unixTimestamp: Long, timezoneOffsetSeconds: Int): String {
        val date = Date(unixTimestamp * 1000)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val offsetMillis = timezoneOffsetSeconds * 1000
        val timeZone = object : TimeZone() {
            override fun getOffset(
                era: Int, year: Int, month: Int, day: Int, dayOfWeek: Int, milliseconds: Int
            ) = offsetMillis

            override fun setRawOffset(offsetMillis: Int) {}
            override fun getRawOffset() = offsetMillis
            override fun useDaylightTime() = false
            override fun inDaylightTime(date: Date?) = false
        }
        format.timeZone = timeZone
        return format.format(date)
    }
}