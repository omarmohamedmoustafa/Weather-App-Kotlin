package com.example.weatherapp.model.local

import com.example.weatherapp.model.pojos.WeatherResponse

class LocalDataSource(private val weatherDao: WeatherDao) {
    suspend fun saveWeatherResponse(response: WeatherResponse) {
        weatherDao.insertHourlyForecastFourDays(response)
    }

    suspend fun getWeatherResponse(): WeatherResponse? {
        return weatherDao.getHourlyForecastFourDays()
    }

    suspend fun clearWeatherResponse() {
        weatherDao.clearHourlyForecastFourDays()
    }

}