package com.example.weatherapp.model.local

import com.example.weatherapp.model.pojos.WeatherResponse

class LocalDataSource(private val weatherDao: WeatherDao) {
    suspend fun saveWeatherResponse(response: WeatherResponse) {
        weatherDao.insertWeatherResponse(response)
    }

    suspend fun getWeatherResponse(): WeatherResponse? {
        return weatherDao.getLatestWeatherResponse()
    }

    suspend fun clearWeatherResponse() {
        weatherDao.clearWeatherResponse()
    }
}