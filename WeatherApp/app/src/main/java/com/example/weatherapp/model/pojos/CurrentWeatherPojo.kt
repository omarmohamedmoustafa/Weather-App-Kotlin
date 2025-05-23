package com.example.weatherapp.model.pojos

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
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
)

data class CurrentCoordinates(
    val lon: Double,
    val lat: Double
)

data class CurrentWeather(
    val id: Long,
    @SerializedName("main")
    val currentWeatherStatus: String,
    @SerializedName("description")
    val currentWeatherDescription: String,
    val icon: String
)

data class CurrentWeatherData(
    val temp: Float,
    @SerializedName("feels_like") val feelsLike: Float,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    val pressure: Long,
    val humidity: Long,
    @SerializedName("sea_level") val seaLevel: Long,
    @SerializedName("grnd_level") val grndLevel: Long
)

data class CurrentWind(
    val speed: Double,
    val deg: Long
//    val gust: Double
)

data class CurrentClouds(
    val all: Long
)

data class CurrentSys(
    val type: Long,
    val id: Long,
    @SerializedName("country")
    val countryCode: String,
    @SerializedName("sunrise")
    val unixSunriseTimeStamp: Long,
    @SerializedName("sunset")
val unixSunsetTimeStamp: Long
)