package com.example.weatherapp.model.local.weather_local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecastFourDays(response: WeatherResponse)

    @Query("SELECT * FROM weather_response ORDER BY id DESC LIMIT 1")
    suspend fun getHourlyForecastFourDays(): WeatherResponse?

    @Query("DELETE FROM weather_response")
    suspend fun clearHourlyForecastFourDays()

    // Supporting Room for CurrentWeatherResponse
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeatherResponse(response: CurrentWeatherResponse)

    @Query("SELECT * FROM current_weather_response ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentWeatherResponse(): CurrentWeatherResponse?

    @Query("DELETE FROM current_weather_response")
    suspend fun clearCurrentWeatherResponse()
}