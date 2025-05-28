package com.example.weatherapp.repository_test

import com.example.weatherapp.model.local.ILocalDataSource
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse

class FakeLocalDataSource(
    private val weatherResponses: MutableMap<Pair<Float, Float>, WeatherResponse> = mutableMapOf(),
    private val currentWeatherResponses: MutableMap<Pair<Float, Float>, CurrentWeatherResponse> = mutableMapOf(),
    private val favouriteCountries: MutableList<FavouriteCountry> = mutableListOf(),
    private val alerts: MutableList<Alert> = mutableListOf(),
) : ILocalDataSource {

    override suspend fun saveWeatherResponse(response: WeatherResponse) {
        weatherResponses.clear() // Only one weather response stored at a time
        weatherResponses[Pair(response.city.coord.lat.toFloat(), response.city.coord.lon.toFloat())] = response
    }

    override suspend fun getWeatherResponse(): WeatherResponse? {
        return weatherResponses.values.firstOrNull()
    }

    override suspend fun clearWeatherResponse() {
        weatherResponses.clear()
    }

    override suspend fun insertFavouriteCountry(country: FavouriteCountry) {
        favouriteCountries.add(country)
    }

    override suspend fun getAllFavouriteCountries(): List<FavouriteCountry> {
        return favouriteCountries.toList()
    }

    override suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float) {
        favouriteCountries.removeIf {
            it.name == countryName && it.latitude == countryLatitude && it.longitude == countryLongitude
        }
    }

    override suspend fun insertCurrentWeatherResponse(response: CurrentWeatherResponse) {
        currentWeatherResponses.clear() // Only one current weather response stored at a time
        currentWeatherResponses[Pair(response.coordinates.lat.toFloat(), response.coordinates.lon.toFloat())] = response
    }

    override suspend fun getCurrentWeatherResponse(): CurrentWeatherResponse? {
        return currentWeatherResponses.values.firstOrNull()
    }

    override suspend fun clearCurrentWeatherResponse() {
        currentWeatherResponses.clear()
    }

    override suspend fun insertAlert(alert: Alert): Long {
        val id = (alerts.size + 1).toLong()
        val alertWithId = alert.copy(alertId = id)
        alerts.add(alertWithId)
        return id
    }

    override suspend fun getAllAlerts(): List<Alert> {
        return alerts.toList()
    }

    override suspend fun deleteAlert(alertId: Long) {
        alerts.removeIf { it.alertId == alertId }
    }

    override suspend fun getAlertById(alertId: Long): Alert? {
        return alerts.find { it.alertId == alertId }
    }
}