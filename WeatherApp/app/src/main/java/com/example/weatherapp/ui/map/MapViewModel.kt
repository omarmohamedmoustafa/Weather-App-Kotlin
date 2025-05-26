package com.example.weatherapp.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private val _selectedLocation = MutableLiveData<Pair<Double, Double>?>()
    val selectedLocation: LiveData<Pair<Double, Double>?> get() = _selectedLocation

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        _selectedLocation.value = Pair(latitude, longitude)
    }

    fun clearSelectedLocation() {
        _selectedLocation.value = null
    }
}