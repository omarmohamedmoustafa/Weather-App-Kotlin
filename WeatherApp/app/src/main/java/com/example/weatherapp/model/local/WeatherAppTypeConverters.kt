package com.example.weatherapp.model.local

import androidx.room.TypeConverter
import com.example.weatherapp.model.pojos.City
import com.example.weatherapp.model.pojos.CurrentClouds
import com.example.weatherapp.model.pojos.CurrentCoordinates
import com.example.weatherapp.model.pojos.CurrentSys
import com.example.weatherapp.model.pojos.CurrentWeather
import com.example.weatherapp.model.pojos.CurrentWeatherData
import com.example.weatherapp.model.pojos.CurrentWind
import com.example.weatherapp.model.pojos.WeatherData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherAppTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeatherDataList(value: List<WeatherData>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeatherDataList(value: String): List<WeatherData> {
        val listType = object : TypeToken<List<WeatherData>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCity(value: City): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCity(value: String): City {
        return gson.fromJson(value, City::class.java)
    }
    @TypeConverter
    fun fromCurrentCoordinates(value: CurrentCoordinates): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentCoordinates(value: String): CurrentCoordinates {
        return gson.fromJson(value, CurrentCoordinates::class.java)
    }

    @TypeConverter
    fun fromCurrentWeatherList(value: List<CurrentWeather>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentWeatherList(value: String): List<CurrentWeather> {
        val listType = object : TypeToken<List<CurrentWeather>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCurrentWeatherData(value: CurrentWeatherData): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentWeatherData(value: String): CurrentWeatherData {
        return gson.fromJson(value, CurrentWeatherData::class.java)
    }

    @TypeConverter
    fun fromCurrentWind(value: CurrentWind): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentWind(value: String): CurrentWind {
        return gson.fromJson(value, CurrentWind::class.java)
    }

    @TypeConverter
    fun fromCurrentClouds(value: CurrentClouds): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentClouds(value: String): CurrentClouds {
        return gson.fromJson(value, CurrentClouds::class.java)
    }

    @TypeConverter
    fun fromCurrentSys(value: CurrentSys): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentSys(value: String): CurrentSys {
        return gson.fromJson(value, CurrentSys::class.java)
    }
}