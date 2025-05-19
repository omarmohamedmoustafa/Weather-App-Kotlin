//package com.example.weatherapp
//
//import android.util.Log
//import com.example.weatherapp.model.remote.RetrofitClient
//import com.example.weatherapp.model.pojos.WeatherResponse
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import retrofit2.Response
//
//object WeatherApiTester {
//    private const val TAG = "WeatherApiTester"
//    fun testWeatherApi(
//        latitude: Float,
//        longitude: Float,
//        apiKey: String,
//        units: String = "metric",
//        language: String = "en"
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response: Response<WeatherResponse> = RetrofitClient.apiService.getWeatherForecast(
//                    latitude = latitude,
//                    longitude = longitude,
//                    apiKey = apiKey,
//                    units = units,
//                    language = language
//                )
//
//                if (response.isSuccessful) {
//                    val weatherResponse = response.body()
//                    if (weatherResponse != null) {
//                        // Log top-level response details
//                        Log.d(TAG, "=== Weather Response ===")
//                        Log.d(TAG, "Code: ${weatherResponse.cod}")
//                        Log.d(TAG, "Message: ${weatherResponse.message}")
//                        Log.d(TAG, "Total Forecasts: ${weatherResponse.cnt}")
//
//                        // Log city details
//                        Log.d(TAG, "=== City ===")
//                        with(weatherResponse.city) {
//                            Log.d(TAG, "ID: $id")
//                            Log.d(TAG, "Name: $name")
//                            Log.d(TAG, "Coordinates: Lat=${coord.lat}, Lon=${coord.lon}")
//                            Log.d(TAG, "Country: $country")
//                            Log.d(TAG, "Population: $population")
//                            Log.d(TAG, "Timezone: $timezone")
//                            Log.d(TAG, "Sunrise: $sunrise")
//                            Log.d(TAG, "Sunset: $sunset")
//                        }
//
//                        // Log all forecast entries
//                        Log.d(TAG, "=== Forecast List ===")
//                        weatherResponse.list.forEachIndexed { index, weatherData ->
//                            Log.d(TAG, "--- Forecast #$index ---")
//                            Log.d(TAG, "DateTime: ${weatherData.dtTxt} (Unix: ${weatherData.dt})")
//                            with(weatherData.main) {
//                                Log.d(TAG, "  Main:")
//                                Log.d(TAG, "    Temp: ${temp}°C")
//                                Log.d(TAG, "    Feels Like: ${feelsLike}°C")
//                                Log.d(TAG, "    Temp Min: ${tempMin}°C")
//                                Log.d(TAG, "    Temp Max: ${tempMax}°C")
//                                Log.d(TAG, "    Pressure: $pressure hPa")
//                                Log.d(TAG, "    Sea Level: $seaLevel hPa")
//                                Log.d(TAG, "    Ground Level: $grndLevel hPa")
//                                Log.d(TAG, "    Humidity: $humidity%")
//                                Log.d(TAG, "    Temp KF: $tempKf")
//                            }
//                            Log.d(TAG, "  Weather:")
//                            weatherData.weather.forEach { weather ->
//                                Log.d(TAG, "    - ID: ${weather.id}")
//                                Log.d(TAG, "      Main: ${weather.main}")
//                                Log.d(TAG, "      Description: ${weather.description}")
//                                Log.d(TAG, "      Icon: ${weather.icon}")
//                            }
//                            Log.d(TAG, "  Clouds: ${weatherData.clouds.all}%")
//                            with(weatherData.wind) {
//                                Log.d(TAG, "  Wind:")
//                                Log.d(TAG, "    Speed: $speed m/s")
//                                Log.d(TAG, "    Direction: $deg°")
//                                Log.d(TAG, "    Gust: $gust m/s")
//                            }
//                            Log.d(TAG, "  Visibility: ${weatherData.visibility} meters")
//                            Log.d(TAG, "  Precipitation Probability: ${weatherData.pop * 100}%")
//                            Log.d(TAG, "  Sys: POD=${weatherData.sys.pod}")
//                        }
//                    } else {
//                        Log.e(TAG, "Response body is null")
//                    }
//                } else {
//                    Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
//                    response.errorBody()?.string()?.let { errorBody ->
//                        Log.e(TAG, "Error Body: $errorBody")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Exception during API call: ${e.message}", e)
//            }
//        }
//    }
//}