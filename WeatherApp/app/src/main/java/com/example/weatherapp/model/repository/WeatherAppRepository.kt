package com.example.weatherapp.model.repository

import com.example.weatherapp.model.local.LocalDataSource
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.remote.RemoteDataSource


class WeatherAppRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {
    // Singleton implementation
    companion object {
        @Volatile
        private var instance: WeatherAppRepository? = null

        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource
        ): WeatherAppRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherAppRepository(remoteDataSource, localDataSource).also { instance = it }
            }
        }
    }

    suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<WeatherResponse> {
        val remoteResult = remoteDataSource.getWeatherForecast(
            latitude = latitude,
            longitude = longitude,
            apiKey = apiKey,
            units = units,
            language = language
        )
        if (remoteResult.isSuccess) {
            remoteResult.getOrNull()?.let { localDataSource.saveWeatherResponse(it) }
            return remoteResult
        }
        // Fallback to local data if network fails
        val localData = localDataSource.getWeatherResponse()
        return if (localData != null) {
            Result.success(localData)
        } else {
            remoteResult
        }
    }

    suspend fun getCurrentWeather(
        latitude: Float,
        longitude: Float,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<CurrentWeatherResponse> {
        val remoteResult = remoteDataSource.getCurrentWeather(
            latitude = latitude,
            longitude = longitude,
            apiKey = apiKey,
            units = units,
            language = language
        )
        if (remoteResult.isSuccess) {
            remoteResult.getOrNull()?.let { localDataSource.insertCurrentWeatherResponse(it) }
            return remoteResult
        }
        // Fallback to local data if network fails
        val localData = localDataSource.getCurrentWeatherResponse()
        return if (localData != null) {
            Result.success(localData)
        } else {
            remoteResult
        }
    }

    @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    )

    suspend fun clearWeatherResponse() {
        localDataSource.clearWeatherResponse()
    }
    suspend fun clearCurrentWeatherResponse() {
        localDataSource.clearCurrentWeatherResponse()
    }

    suspend fun insertFavouriteCountry(country: FavouriteCountry) {
        localDataSource.insertFavouriteCountry(country)
    }
    suspend fun getAllFavouriteCountries(): List<FavouriteCountry>{
        return localDataSource.getAllFavouriteCountries()
    }
    suspend fun deleteFavouriteCountry(countryName: String, countryLatitude: Float, countryLongitude: Float){
        localDataSource.deleteFavouriteCountry(countryName, countryLatitude, countryLongitude)
    }

}