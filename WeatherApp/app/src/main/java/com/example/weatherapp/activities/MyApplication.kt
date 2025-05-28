package com.example.weatherapp.activities

import com.example.weatherapp.model.local.LocalDataSource
import com.example.weatherapp.model.local.WeatherAppDatabase
import com.example.weatherapp.model.remote.RemoteDataSource
import com.example.weatherapp.model.repository.WeatherAppRepository
import android.app.Application


class MyApplication : Application() {

    lateinit var weatherRepository: WeatherAppRepository
        private set

    override fun onCreate() {
        super.onCreate()

        weatherRepository = WeatherAppRepository.getInstance(
            remoteDataSource = RemoteDataSource(),
            localDataSource = LocalDataSource(
                WeatherAppDatabase.getDatabase(this).weatherDao()
            )
        )
    }
}