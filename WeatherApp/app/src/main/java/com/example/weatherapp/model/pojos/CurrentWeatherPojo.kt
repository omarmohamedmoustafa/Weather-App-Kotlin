package com.example.weatherapp.model.pojos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherapp.model.local.WeatherAppTypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(tableName = "current_weather_response")
@TypeConverters(WeatherAppTypeConverters::class)
@Parcelize
data class CurrentWeatherResponse(
    @PrimaryKey(autoGenerate = true) val currentId: Long = 0,
    @SerializedName("coord")
    val coordinates: CurrentCoordinates,
    @SerializedName("weather")
    val currentWeather: List<CurrentWeather>,
    val base: String,
    @SerializedName("main")
    val weatherData: CurrentWeatherData,
    val visibility: Long,
    val wind: CurrentWind,
    val clouds: CurrentClouds,
    @SerializedName("dt")
    val unixTimeStamp: Long,
    val sys: CurrentSys,
    @SerializedName("timezone")
    val offsetFromUTC: Long,
    val id: Long,
    @SerializedName("name")
    val currentLocationName: String,
    @SerializedName("cod")
    val responseCode: Long
): Parcelable

@Parcelize
data class CurrentCoordinates(
    val lon: Double,
    val lat: Double
) : Parcelable

@Parcelize
data class CurrentWeather(
    val id: Long,
    @SerializedName("main")
    val currentWeatherStatus: String,
    @SerializedName("description")
    val currentWeatherDescription: String,
    val icon: String
) : Parcelable

@Parcelize
data class CurrentWeatherData(
    val temp: Float,
    @SerializedName("feels_like") val feelsLike: Float,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    val pressure: Long,
    val humidity: Long,
    @SerializedName("sea_level") val seaLevel: Long,
    @SerializedName("grnd_level") val grndLevel: Long
) : Parcelable

@Parcelize
data class CurrentWind(
    val speed: Double,
    val deg: Long
) : Parcelable

@Parcelize
data class CurrentClouds(
    val all: Long
) : Parcelable

@Parcelize
data class CurrentSys(
    val type: Long,
    val id: Long,
    @SerializedName("country")
    val countryCode: String,
    @SerializedName("sunrise")
    val unixSunriseTimeStamp: Long,
    @SerializedName("sunset")
    val unixSunsetTimeStamp: Long
) : Parcelable