package com.example.weatherapp.model.pojos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_countries")
data class FavouriteCountry(
    @PrimaryKey
    val name: String,
    val latitude: Float,
    val longitude: Float,
    val minTemp: Float,
    val maxTemp: Float,
    val weatherIcon: String?,
    val flagUrl: String
)