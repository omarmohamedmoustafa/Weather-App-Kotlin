package com.example.weatherapp.model.local.fav_local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherapp.model.pojos.FavouriteCountry

@Database(entities = [FavouriteCountry::class], version = 1, exportSchema = false)
abstract class FavCountriesDatabase : RoomDatabase() {
    abstract fun favDao(): FavDao

    companion object {
        @Volatile
        private var INSTANCE: FavCountriesDatabase? = null

        fun getDatabase(context: Context): FavCountriesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavCountriesDatabase::class.java,
                    "favourite_countries_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}