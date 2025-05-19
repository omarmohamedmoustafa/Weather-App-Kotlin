package com.example.mvvm.model.repository

import com.example.weatherapp.model.local.LocalDataSource
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.remote.RemoteDataSource

class WeatherRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {
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
}