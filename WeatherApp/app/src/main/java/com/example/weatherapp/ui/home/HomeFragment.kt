package com.example.weatherapp.ui.home

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.MainActivity
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.repository.WeatherRepository
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(getWeatherRepository(), (requireActivity() as MainActivity).locationHelper)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView for hourly forecast
        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Set up RecyclerView for daily forecast
        binding.rvDailyForecast.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        // Fetch weather data using location
        viewModel.fetchWeatherDataWithLocation(
            apiKey = "7dcce3c6b65dedcdd3bd946e7b4c20f2"
        )

        // Observe current weather
        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weather ->
            weather?.let {
                Log.d("HomeFragment", "Updating UI with current weather: $weather")
                updateWeatherUI(it)
            }
        })

        // Observe forecast weather
        viewModel.forecastWeather.observe(viewLifecycleOwner, Observer { forecast ->
            forecast?.let {
                Log.d("HomeFragment", "Updating UI with forecast: $forecast")
                // Filter hourly forecast list to show only the next 24 hours
                val currentTimeMillis = System.currentTimeMillis() / 1000 // Current time in seconds
                val twentyFourHoursLater = currentTimeMillis + (24 * 60 * 60) // 24 hours in seconds
                val filteredHourlyForecastList = forecast.list.filter { weatherData ->
                    weatherData.unixTimeStamp in (currentTimeMillis + 1)..twentyFourHoursLater
                }

                // Log hourly filtered list for debugging
                Log.d("HomeFragment", "Filtered hourly forecast list size: ${filteredHourlyForecastList.size}")
                filteredHourlyForecastList.forEach { weatherData ->
                    Log.d("HomeFragment", "Hourly forecast time: ${viewModel.formatUnixTimeWithOffset(weatherData.unixTimeStamp, forecast.city.timezone)}")
                }

                // Show a message if the hourly filtered list is empty
                if (filteredHourlyForecastList.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "No hourly forecast data available for the next 24 hours",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                val hourlyAdapter = HourlyForecastAdapter(
                    filteredHourlyForecastList,
                    forecast.city.timezone,
                    viewModel::formatHourlyTime
                )
                binding.rvHourlyForecast.adapter = hourlyAdapter

                // Filter and group daily forecast for the next 4 days with aggregated min/max
                val fourDaysLater = currentTimeMillis + (4 * 24 * 60 * 60) // 4 days in seconds
                val filteredDailyForecastList = forecast.list
                    .filter { weatherData ->
                        weatherData.unixTimeStamp in (currentTimeMillis + 1)..fourDaysLater
                    }
                    .groupBy { weatherData ->
                        // Group by day in local time
                        val date = Date(weatherData.unixTimeStamp * 1000)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("GMT").apply { rawOffset = forecast.city.timezone * 1000 }
                        }
                        dateFormat.format(date)
                    }
                    .mapValues { entry ->
                        // Aggregate min and max temperatures for the day
                        val minTemp = entry.value.minOf { it.weatherDataOfHour.tempMin }
                        val maxTemp = entry.value.maxOf { it.weatherDataOfHour.tempMax }
                        // Use the first entry as a base, override tempMin and tempMax
                        entry.value.first().copy(
                            weatherDataOfHour = entry.value.first().weatherDataOfHour.copy(
                                tempMin = minTemp,
                                tempMax = maxTemp
                            )
                        )
                    }
                    .values
                    .take(4) // Ensure only 4 days are selected
                    .toList()

                // Log daily filtered list for debugging
                Log.d("HomeFragment", "Filtered daily forecast list size: ${filteredDailyForecastList.size}")
                filteredDailyForecastList.forEach { weatherData ->
                    Log.d("HomeFragment", "Daily forecast time: ${viewModel.formatUnixTimeWithOffset(weatherData.unixTimeStamp, forecast.city.timezone)}, Min/Max: ${weatherData.weatherDataOfHour.tempMin}/${weatherData.weatherDataOfHour.tempMax}")
                }

                // Show a message if the daily filtered list is empty
                if (filteredDailyForecastList.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "No daily forecast data available for the next 4 days",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                val dailyAdapter = DailyForecastAdapter(
                    filteredDailyForecastList,
                    forecast.city.timezone
                )
                binding.rvDailyForecast.adapter = dailyAdapter
            }
        })

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("HomeFragment", "Error: $error")
                Snackbar.make(
                    binding.root,
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun updateWeatherUI(weather: CurrentWeatherResponse) {
        binding.tvCity.text = weather.currentLocationName
        val (date, time) = viewModel.formatDateTime(weather.unixTimeStamp, weather.offsetFromUTC)
        binding.tvDateTime.text = "$date • $time"
        binding.tvTemperature.text = String.format("%.1f°C", weather.weatherData.temp)
        val weatherDescription = weather.currentWeather.firstOrNull()
        binding.tvWeatherDescription.text = weatherDescription?.currentWeatherDescription?.replaceFirstChar {
            it.uppercaseChar()
        }
        weatherDescription?.icon?.let { iconCode ->
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            Glide.with(this)
                .load(iconUrl)
                .into(binding.ivWeatherIcon)
        }
        binding.tvHumidity.text = "${weather.weatherData.humidity}%"
        binding.tvWindSpeed.text = "${weather.wind.speed} m/s"
        binding.tvPressure.text = "${weather.weatherData.pressure} hPa"
        binding.tvClouds.text = "${weather.clouds.all}%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getWeatherRepository(): WeatherRepository {
        return (requireActivity() as MainActivity).weatherRepository
    }
}