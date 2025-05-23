package com.example.weatherapp.model.repository

import com.example.weatherapp.model.local.LocalDataSource
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.remote.RemoteDataSource

class WeatherRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {
    //singleton implementation
    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource
        ): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository(remoteDataSource, localDataSource).also { instance = it }
            }
        }
    }

    suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<WeatherResponse> {
        val remoteResult = remoteDataSource.getWeatherForecast(
            latitude = latitude,
            longitude = longitude,
            apiKey = apiKey,
            units = units,
            language = language
        )
        if (remoteResult.isSuccess) {
            remoteResult.getOrNull()?.let { localDataSource.saveWeatherResponse(it) }
            return remoteResult
        }
        // Fallback to local data if network fails
        val localData = localDataSource.getWeatherResponse()
        return if (localData != null) {
            Result.success(localData)
        } else {
            remoteResult
        }
    }

    suspend fun getCurrentWeather(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<CurrentWeatherResponse> {
        val remoteResult = remoteDataSource.getCurrentWeather(
            latitude = latitude,
            longitude = longitude,
            apiKey = apiKey,
            units = units,
            language = language
        )
        // I don't need to fallback to local data for current weather. I need to show the data fetched from the API
        // even if it is not successful. So I will return the remote result directly.
        return remoteResult
    }
}