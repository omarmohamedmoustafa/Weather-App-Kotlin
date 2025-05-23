package com.example.weatherapp.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.model.pojos.WeatherResponse

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecastFourDays(response: WeatherResponse)

    @Query("SELECT * FROM weather_response ORDER BY id DESC LIMIT 1")
    suspend fun getHourlyForecastFourDays(): WeatherResponse?

    @Query("DELETE FROM weather_response")
    suspend fun clearHourlyForecastFourDays()

}