package com.example.weatherapp.model.remote


import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse

interface IRemoteDataSource {
    suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String,
        language: String
    ): Result<WeatherResponse>

    suspend fun getCurrentWeather(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String,
        language: String
    ): Result<CurrentWeatherResponse>
}