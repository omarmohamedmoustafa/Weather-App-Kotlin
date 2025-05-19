package com.example.weatherapp.model.remote

import com.example.weatherapp.model.pojos.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RemoteDataSource {
    suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String,
        language: String
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            val response: Response<WeatherResponse> = RetrofitClient.apiService.getWeatherForecast(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API call failed with code ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}