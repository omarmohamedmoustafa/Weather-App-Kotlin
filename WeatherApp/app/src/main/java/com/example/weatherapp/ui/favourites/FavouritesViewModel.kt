package com.example.weatherapp.ui.favourites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.key._apiKey
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.repository.WeatherAppRepository
import kotlinx.coroutines.launch

class FavouritesViewModel(private val repository: WeatherAppRepository) : ViewModel() {
    private val _favoriteCountries = MutableLiveData<List<FavouriteCountry>>()
    val favoriteCountries: LiveData<List<FavouriteCountry>> get() = _favoriteCountries

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        loadFavoriteCountries()
    }

    private fun loadFavoriteCountries() {
        viewModelScope.launch {
            try {
                val countries = repository.getAllFavouriteCountries()
                _favoriteCountries.value = countries
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load favorite countries"
            }
        }
    }

    fun addFavoriteCountry(latitude: Float, longitude: Float) {
        viewModelScope.launch {
            try {
                val weatherResult = repository.getCurrentWeather(latitude, longitude, _apiKey)
                weatherResult.getOrNull()?.let { weather ->
                    val country = FavouriteCountry(
                        name = weather.currentLocationName,
                        latitude = latitude,
                        longitude = longitude,
                        minTemp = weather.weatherData.temp, // Simplified; adjust as needed
                        maxTemp = weather.weatherData.temp,
                        weatherIcon = weather.currentWeather.firstOrNull()?.icon,
                        flagUrl = "" // Add flag URL logic if needed
                    )
                    repository.insertFavouriteCountry(country)
                    loadFavoriteCountries() // Refresh list
                } ?: throw Exception("Failed to fetch weather data")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add favorite country"
            }
        }
    }

    fun deleteFavoriteCountry(countryName: String, latitude: Float, longitude: Float) {
        viewModelScope.launch {
            try {
                repository.deleteFavouriteCountry(countryName, latitude, longitude)
                loadFavoriteCountries() // Refresh list after deletion
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete favorite country"
            }
        }
    }
}