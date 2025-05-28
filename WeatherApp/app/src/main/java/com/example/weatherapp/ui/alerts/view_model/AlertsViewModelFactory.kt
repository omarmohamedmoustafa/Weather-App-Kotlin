package com.example.weatherapp.ui.alerts.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.model.alert.AlertsHelper
import com.example.weatherapp.model.repository.WeatherAppRepository

class AlertsViewModelFactory(
    private val repository: WeatherAppRepository,
    private val alertsHelper: AlertsHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(repository, alertsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}