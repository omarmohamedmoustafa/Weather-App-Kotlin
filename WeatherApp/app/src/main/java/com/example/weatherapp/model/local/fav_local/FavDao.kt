package com.example.weatherapp.model.local.fav_local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.weatherapp.model.pojos.FavouriteCountry


@Dao
interface FavDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertFavouriteCountry(country: FavouriteCountry)

    @Query("SELECT * FROM favorite_countries")
    suspend fun getAllFavouriteCountries(): List<FavouriteCountry>

    @Query("DELETE FROM favorite_countries WHERE name = :countryName AND latitude = :countryLatitude AND longitude = :countryLongitude")
    suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float)
}