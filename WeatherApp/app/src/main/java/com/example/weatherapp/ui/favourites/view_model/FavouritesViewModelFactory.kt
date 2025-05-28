package com.example.weatherapp.ui.favourites.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.model.repository.WeatherAppRepository

class FavouritesViewModelFactory(
    private val repository: WeatherAppRepository
) :ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouritesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
