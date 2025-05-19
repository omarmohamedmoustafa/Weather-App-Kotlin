package com.example.weatherapp.model.remote

import com.example.weatherapp.model.pojos.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("forecast/hourly")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Float,
        @Query("lon") longitude: Float,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "en"
    ): Response<WeatherResponse>
}