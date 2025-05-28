package com.example.weatherapp.repository_test

import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.remote.IRemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk

class FakeRemoteDataSource(
    private val weatherResponses: MutableMap<Pair<Float, Float>, WeatherResponse> = mutableMapOf(),
    private val currentWeatherResponses: MutableMap<Pair<Float, Float>, CurrentWeatherResponse> = mutableMapOf(),
) : IRemoteDataSource {

    // Create a mock instance for IRemoteDataSource
    private val remoteDataSourceMock: IRemoteDataSource = mockk<IRemoteDataSource>().apply {
        coEvery {
            getWeatherForecast(
                latitude = any(),
                longitude = any(),
                apiKey = any(),
                units = any(),
                language = any()
            )
        } answers {
            val latitude = firstArg<Float>()
            val longitude = secondArg<Float>()
            weatherResponses[Pair(latitude, longitude)]?.let { Result.success<WeatherResponse>(it) }
                ?: Result.failure(Exception("No weather forecast for coordinates ($latitude, $longitude)"))
        }

        coEvery {
            getCurrentWeather(
                latitude = any(),
                longitude = any(),
                apiKey = any(),
                units = any(),
                language = any()
            )
        } answers {
            val latitude = firstArg<Float>()
            val longitude = secondArg<Float>()
            currentWeatherResponses[Pair(latitude, longitude)]?.let { Result.success<CurrentWeatherResponse>(it) }
                ?: Result.failure(Exception("No current weather for coordinates ($latitude, $longitude)"))
        }
    }

    override suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String,
        language: String
    ): Result<WeatherResponse> {
        return remoteDataSourceMock.getWeatherForecast(latitude, longitude, apiKey, units, language)
    }

    override suspend fun getCurrentWeather(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String,
        language: String
    ): Result<CurrentWeatherResponse> {
        return remoteDataSourceMock.getCurrentWeather(latitude, longitude, apiKey, units, language)
    }
}