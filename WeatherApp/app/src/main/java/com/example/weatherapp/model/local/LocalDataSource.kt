package com.example.weatherapp.model.local

import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSource(
    private val weatherAppDao: WeatherAppDao
): ILocalDataSource {

    override suspend fun saveWeatherResponse(response: WeatherResponse) = withContext(Dispatchers.IO) {
        weatherAppDao.insertHourlyForecastFourDays(response)
    }

    override suspend fun getWeatherResponse(): WeatherResponse? = withContext(Dispatchers.IO) {
        return@withContext weatherAppDao.getHourlyForecastFourDays()
    }

    override suspend fun clearWeatherResponse() = withContext(Dispatchers.IO) {
        weatherAppDao.clearHourlyForecastFourDays()
    }

    override suspend fun insertFavouriteCountry(country: FavouriteCountry) = withContext(Dispatchers.IO) {
        weatherAppDao.insertFavouriteCountry(country)
    }

    override suspend fun getAllFavouriteCountries(): List<FavouriteCountry> = withContext(Dispatchers.IO) {
        return@withContext weatherAppDao.getAllFavouriteCountries()
    }

    override suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float) = withContext(Dispatchers.IO) {
        weatherAppDao.deleteFavouriteCountry(countryName, countryLatitude, countryLongitude)
    }

    override suspend fun insertCurrentWeatherResponse(response: CurrentWeatherResponse) = withContext(Dispatchers.IO) {
        weatherAppDao.insertCurrentWeatherResponse(response)
    }

    override suspend fun getCurrentWeatherResponse(): CurrentWeatherResponse? = withContext(Dispatchers.IO) {
        return@withContext weatherAppDao.getCurrentWeatherResponse()
    }

    override suspend fun clearCurrentWeatherResponse() = withContext(Dispatchers.IO) {
        weatherAppDao.clearCurrentWeatherResponse()
    }

    override suspend fun insertAlert(alert: Alert): Long= withContext(Dispatchers.IO) {
        return@withContext weatherAppDao.insertAlert(alert)
    }
    override suspend fun getAllAlerts(): List<Alert> = withContext(Dispatchers.IO) {
        weatherAppDao.getAllAlerts()
    }
    override suspend fun deleteAlert(alertId: Long) = withContext(Dispatchers.IO) {
        weatherAppDao.deleteAlert(alertId)
    }
    override suspend fun getAlertById(alertId: Long): Alert? = withContext(Dispatchers.IO) {
        return@withContext weatherAppDao.getAlertById(alertId)
    }
}