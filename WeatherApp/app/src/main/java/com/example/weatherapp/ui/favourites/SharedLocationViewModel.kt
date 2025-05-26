package com.example.weatherapp.ui.favourites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedLocationViewModel : ViewModel() {
    private val _selectedCoordinates = MutableLiveData<Pair<Float, Float>?>(null)
    val selectedCoordinates: LiveData<Pair<Float, Float>?> get() = _selectedCoordinates

    fun setSelectedCoordinates(latitude: Float, longitude: Float) {
        _selectedCoordinates.value = Pair(latitude, longitude)
    }

    fun clearSelectedCoordinates() {
        _selectedCoordinates.value = null
    }
}