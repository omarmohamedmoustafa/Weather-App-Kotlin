package com.example.weatherapp.model.local

import com.example.weatherapp.model.local.fav_local.FavDao
import com.example.weatherapp.model.local.weather_local.WeatherDao
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSource(private val weatherDao: WeatherDao,
                      private val favDao: FavDao)  {

    suspend fun saveWeatherResponse(response: WeatherResponse) = withContext(Dispatchers.IO) {
        weatherDao.insertHourlyForecastFourDays(response)
    }

    suspend fun getWeatherResponse(): WeatherResponse? = withContext(Dispatchers.IO) {
        return@withContext weatherDao.getHourlyForecastFourDays()
    }

    suspend fun clearWeatherResponse() = withContext(Dispatchers.IO) {
        weatherDao.clearHourlyForecastFourDays()
    }

    suspend fun insertFavouriteCountry(country: FavouriteCountry) = withContext(Dispatchers.IO) {
        favDao.insertFavouriteCountry(country)
    }

    suspend fun getAllFavouriteCountries(): List<FavouriteCountry> = withContext(Dispatchers.IO) {
        return@withContext favDao.getAllFavouriteCountries()
    }

    suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float) = withContext(Dispatchers.IO) {
        favDao.deleteFavouriteCountry(countryName, countryLatitude, countryLongitude)
    }

    suspend fun insertCurrentWeatherResponse(response: CurrentWeatherResponse) = withContext(Dispatchers.IO) {
        weatherDao.insertCurrentWeatherResponse(response)
    }

    suspend fun getCurrentWeatherResponse(): CurrentWeatherResponse? = withContext(Dispatchers.IO) {
        return@withContext weatherDao.getCurrentWeatherResponse()
    }

    suspend fun clearCurrentWeatherResponse() = withContext(Dispatchers.IO) {
        weatherDao.clearCurrentWeatherResponse()
    }
}