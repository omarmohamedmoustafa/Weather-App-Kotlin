package com.example.weatherapp.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse

@Dao
interface WeatherAppDao {

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavouriteCountry(country: FavouriteCountry)

    @Query("SELECT * FROM favorite_countries")
    suspend fun getAllFavouriteCountries(): List<FavouriteCountry>

    @Query("DELETE FROM favorite_countries WHERE name = :countryName AND latitude = :countryLatitude AND longitude = :countryLongitude")
    suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert): Long

    @Query("SELECT * FROM alerts")
    suspend fun getAllAlerts(): List<Alert>

    @Query("DELETE FROM alerts WHERE alertId = :alertId")
    suspend fun deleteAlert(alertId: Long)

    @Query("SELECT * FROM alerts WHERE alertId = :alertId")
    suspend fun getAlertById(alertId: Long): Alert?

}