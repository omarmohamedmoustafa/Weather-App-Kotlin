package com.example.weatherapp.ui.alerts.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.alert.AlertsHelper
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.repository.WeatherAppRepository
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: WeatherAppRepository,
    private val alertsHelper: AlertsHelper
) : ViewModel() {
    private val _alarms = MutableLiveData<List<Alert>>()
    val alarms: LiveData<List<Alert>> get() = _alarms

    init {
        getAllAlarms()
    }

    fun getAllAlarms() {
        viewModelScope.launch {
            val result = repository.getAllAlerts()
            _alarms.value = result
        }
    }

    fun deleteAlarm(alert: Alert) {
        viewModelScope.launch {
            repository.deleteAlert(alert.alertId)
            alertsHelper.cancelAlarm(alert)
            getAllAlarms()
        }
    }

    fun addAlarm(alert: Alert) {
        viewModelScope.launch {
            val alarmId = repository.insertAlert(alert)
            val savedAlert = alert.copy(alertId = alarmId)
            alertsHelper.setAlarm(savedAlert)
            getAllAlarms()
        }
    }
}