package com.example.weatherapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.key._apiKey
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.repository.WeatherAppRepository
import com.example.weatherapp.ui.favourites.SharedLocationViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(getWeatherRepository())
    }

    private val sharedLocationViewModel: SharedLocationViewModel by viewModels({ requireActivity() })

    // Store coordinates to prevent re-fetching on configuration changes
    private var selectedLatitude: Float? = null
    private var selectedLongitude: Float? = null
    private var isFavoriteLocation: Boolean = false

    // SharedPreferences for settings
    private lateinit var settingsPrefs: SharedPreferences

    // Settings values
    private var locationSetting: String? = null
    private var languageSetting: String? = null
    private var temperatureSetting: String? = null
    private var windSpeedSetting: String? = null
    private var notificationsSetting: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.POST_NOTIFICATIONS])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences for settings
        settingsPrefs = requireContext().getSharedPreferences("SettingsPrefs", android.content.Context.MODE_PRIVATE)

        // Retrieve settings from SharedPreferences
        locationSetting = settingsPrefs.getString("location", "GPS")
        languageSetting = settingsPrefs.getString("language", "English")
        temperatureSetting = settingsPrefs.getString("temperature", "Celsius")
        windSpeedSetting = settingsPrefs.getString("wind_speed", "Meter/Sec")
        notificationsSetting = settingsPrefs.getString("notifications", "Disable")

        // Log retrieved settings for debugging
        Log.d("HomeFragment", "Settings retrieved: location=$locationSetting, language=$languageSetting, " +
                "temperature=$temperatureSetting, windSpeed=$windSpeedSetting, notifications=$notificationsSetting")

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

        // Set up SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Trigger refresh when user swipes down
            refreshWeatherData()
        }

        // Restore coordinates if available (e.g., after configuration change)
        if (selectedLatitude != null && selectedLongitude != null) {
            fetchWeatherData(selectedLatitude!!, selectedLongitude!!, isFavoriteLocation)
            return
        }

        // Observe selected coordinates
        sharedLocationViewModel.selectedCoordinates.observe(viewLifecycleOwner, Observer { coordinates ->
            if (coordinates != null) {
                // Use coordinates from SharedLocationViewModel (from FavouritesFragment)
                selectedLatitude = coordinates.first
                selectedLongitude = coordinates.second
                isFavoriteLocation = true
                fetchWeatherData(selectedLatitude!!, selectedLongitude!!, isFavoriteLocation)
            } else {
                // Fallback to SharedPreferences for home location
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                selectedLatitude = sharedPreferences.getFloat("home_latitude", 0f)
                selectedLongitude = sharedPreferences.getFloat("home_longitude", 0f)
                isFavoriteLocation = false
                if (selectedLatitude == 0f && selectedLongitude == 0f) {
                    Snackbar.make(
                        binding.root,
                        "No home location set. Please set a location in settings.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                    return@Observer
                }
                fetchWeatherData(selectedLatitude!!, selectedLongitude!!, isFavoriteLocation)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Hide FAB for both home and favorite locations
        (requireActivity() as MainActivity).getFab().visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        // Reset to home location if showing a favorite location
        if (isFavoriteLocation) {
            sharedLocationViewModel.clearSelectedCoordinates()
            selectedLatitude = null
            selectedLongitude = null
            isFavoriteLocation = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshWeatherData() {
        // Check if coordinates are available
        if (selectedLatitude != null && selectedLongitude != null) {
            fetchWeatherData(selectedLatitude!!, selectedLongitude!!, isFavoriteLocation)
        } else {
            // Fallback to SharedPreferences for home location
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            selectedLatitude = sharedPreferences.getFloat("home_latitude", 0f)
            selectedLongitude = sharedPreferences.getFloat("home_longitude", 0f)
            isFavoriteLocation = false
            if (selectedLatitude == 0f && selectedLongitude == 0f) {
                Snackbar.make(
                    binding.root,
                    "No location set. Please set a location in settings.",
                    Snackbar.LENGTH_LONG
                ).show()
                binding.swipeRefreshLayout.isRefreshing = false
                return
            }
            fetchWeatherData(selectedLatitude!!, selectedLongitude!!, isFavoriteLocation)
        }
    }

    private fun fetchWeatherData(latitude: Float, longitude: Float, isFavoriteLocation: Boolean) {
        // Fetch weather data using the selected coordinates
        viewModel.fetchWeatherData(
            latitude = latitude,
            longitude = longitude,
            apiKey = _apiKey,
            language = when(languageSetting) {
                "English" -> "en"
                "Arabic" -> "ar"
                else -> "en"
            },
            units = when (temperatureSetting) {
                "Celsius" -> "metric"
                "Fahrenheit" -> "imperial"
                else -> "standard"
            }
        )

        // Observe current weather
        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weather ->
            weather?.let {
                updateWeatherUI(it)
                // Stop the refreshing animation when data is loaded
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        // Observe forecast weather
        viewModel.forecastWeather.observe(viewLifecycleOwner, Observer { forecast ->
            forecast?.let {
                // Filter hourly forecast list to show only the next 24 hours
                val currentTimeMillis = System.currentTimeMillis() / 1000
                val twentyFourHoursLater = currentTimeMillis + (24 * 60 * 60)
                val filteredHourlyForecastList = forecast.list.filter { weatherData ->
                    weatherData.unixTimeStamp in (currentTimeMillis + 1)..twentyFourHoursLater
                }

                // Show a message if the hourly filtered list is empty
                if (filteredHourlyForecastList.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "No hourly forecast data available for the next 24 hours",
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                val hourlyAdapter = HourlyForecastAdapter(
                    forecast.city.timezone,
                    viewModel::formatHourlyTime
                )
                binding.rvHourlyForecast.adapter = hourlyAdapter
                hourlyAdapter.submitList(filteredHourlyForecastList)

                // Filter and group daily forecast for the next 4 days with aggregated min/max
                val fourDaysLater = currentTimeMillis + (4 * 24 * 60 * 60)
                val filteredDailyForecastList = forecast.list
                    .filter { weatherData ->
                        weatherData.unixTimeStamp in (currentTimeMillis + 1)..fourDaysLater
                    }
                    .groupBy { weatherData ->
                        val date = Date(weatherData.unixTimeStamp * 1000)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("GMT").apply { rawOffset = forecast.city.timezone * 1000 }
                        }
                        dateFormat.format(date)
                    }
                    .mapValues { entry ->
                        val minTemp = entry.value.minOf { it.weatherDataOfHour.tempMin }
                        val maxTemp = entry.value.maxOf { it.weatherDataOfHour.tempMax }
                        entry.value.first().copy(
                            weatherDataOfHour = entry.value.first().weatherDataOfHour.copy(
                                tempMin = minTemp,
                                tempMax = maxTemp
                            )
                        )
                    }
                    .values
                    .take(4)
                    .toList()

                // Show a message if the daily filtered list is empty
                if (filteredDailyForecastList.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "No daily forecast data available for the next 4 days",
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                val dailyAdapter = DailyForecastAdapter(
                    forecast.city.timezone
                )
                binding.rvDailyForecast.adapter = dailyAdapter
                dailyAdapter.submitList(filteredDailyForecastList)

                // Stop the refreshing animation when data is loaded
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(
                    binding.root,
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        // Observe loading state
//        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            // Ensure SwipeRefreshLayout reflects loading state
//            if (isLoading) {
//                binding.swipeRefreshLayout.isRefreshing = true
//            }
//        })
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
            val resourceName = "ic_$iconCode"
            val resourceId = context?.resources?.getIdentifier(
                resourceName,
                "drawable",
                context?.packageName
            ) ?: 0
            if (resourceId != 0) {
                Glide.with(this)
                    .load(resourceId)
                    .into(binding.ivWeatherIcon)
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_launcher_background)
                    .into(binding.ivWeatherIcon)
            }
        }
        binding.tvHumidity.text = "${weather.weatherData.humidity}%"
        binding.tvWindSpeed.text = "${weather.wind.speed} m/s"
        binding.tvPressure.text = "${weather.weatherData.pressure} hPa"
        binding.tvClouds.text = "${weather.clouds.all}%"
    }

    private fun getWeatherRepository(): WeatherAppRepository {
        return (requireActivity() as MainActivity).weatherRepository
    }
}