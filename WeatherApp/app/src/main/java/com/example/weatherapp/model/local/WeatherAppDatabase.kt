package com.example.weatherapp.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse

@Database(entities = [WeatherResponse::class, CurrentWeatherResponse::class, Alert::class, FavouriteCountry::class], version = 1, exportSchema = false)
@TypeConverters(WeatherAppTypeConverters::class)
abstract class WeatherAppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherAppDao
    companion object {
        @Volatile
        private var INSTANCE: WeatherAppDatabase? = null
        fun getDatabase(context: Context): WeatherAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherAppDatabase::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}