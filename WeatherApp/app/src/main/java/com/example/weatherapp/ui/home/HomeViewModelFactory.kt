package com.example.weatherapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.model.location.LocationHelper
import com.example.weatherapp.model.repository.WeatherRepository

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, locationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}