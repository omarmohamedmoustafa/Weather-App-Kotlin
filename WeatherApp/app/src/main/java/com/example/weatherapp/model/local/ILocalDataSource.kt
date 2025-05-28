package com.example.weatherapp.model.local
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse

interface ILocalDataSource {
    suspend fun saveWeatherResponse(response: WeatherResponse)
    suspend fun getWeatherResponse(): WeatherResponse?
    suspend fun clearWeatherResponse()
    suspend fun insertFavouriteCountry(country: FavouriteCountry)
    suspend fun getAllFavouriteCountries(): List<FavouriteCountry>
    suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float)
    suspend fun insertCurrentWeatherResponse(response: CurrentWeatherResponse)
    suspend fun getCurrentWeatherResponse(): CurrentWeatherResponse?
    suspend fun clearCurrentWeatherResponse()
    suspend fun insertAlert(alert: Alert): Long
    suspend fun getAllAlerts(): List<Alert>
    suspend fun deleteAlert(alertId: Long)
    suspend fun getAlertById(alertId: Long): Alert?
}