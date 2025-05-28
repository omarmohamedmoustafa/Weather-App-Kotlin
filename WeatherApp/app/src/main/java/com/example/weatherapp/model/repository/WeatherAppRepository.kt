package com.example.weatherapp.model.repository

import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.remote.IRemoteDataSource
import com.example.weatherapp.model.local.ILocalDataSource


class WeatherAppRepository(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource
) {
    companion object {
        @Volatile
        private var instance: WeatherAppRepository? = null

        fun getInstance(
            remoteDataSource: IRemoteDataSource,
            localDataSource: ILocalDataSource
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

    suspend fun saveWeatherResponse(response: WeatherResponse) {
        localDataSource.saveWeatherResponse(response)
    }
    suspend fun getWeatherResponse(): WeatherResponse? {
        return localDataSource.getWeatherResponse()
    }
    suspend fun saveCurrentWeatherResponse(response: CurrentWeatherResponse) {
        localDataSource.insertCurrentWeatherResponse(response)
    }
    suspend fun getCurrentWeatherResponse(): CurrentWeatherResponse? {
        return localDataSource.getCurrentWeatherResponse()
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

    suspend fun insertAlert(alert: Alert): Long {
        return localDataSource.insertAlert(alert)
    }

    suspend fun getAllAlerts(): List<Alert> {
        return localDataSource.getAllAlerts()
    }

    suspend fun deleteAlert(alertId: Long) {
        localDataSource.deleteAlert(alertId)
    }

    suspend fun getAlertById(alertId: Long): Alert? {
        return localDataSource.getAlertById(alertId)
    }


}