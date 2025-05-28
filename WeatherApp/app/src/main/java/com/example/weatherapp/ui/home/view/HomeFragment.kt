package com.example.weatherapp.ui.home.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.activities.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.key._apiKey
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.repository.WeatherAppRepository
import com.example.weatherapp.ui.favourites.view_model.SharedLocationViewModel
import com.example.weatherapp.ui.home.view_model.HomeViewModel
import com.example.weatherapp.ui.home.view_model.HomeViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import com.example.weatherapp.utils.UnitConverter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(getWeatherRepository())
    }

    private val sharedLocationViewModel: SharedLocationViewModel by viewModels(
        {
            requireActivity()
        }
    )

    private var selectedLatitude: Float? = null
    private var selectedLongitude: Float? = null
    private var isFavoriteLocation: Boolean = false

    private lateinit var homePrefs: SharedPreferences
    private var languageSetting: String? = null
    private var temperatureSetting: String? = null
    private var windSpeedSetting: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homePrefs = requireContext().getSharedPreferences("HomeSettingsPrefs", Context.MODE_PRIVATE)

        // Retrieve settings from SharedPreferences
        languageSetting = homePrefs.getString("language", "en")
        temperatureSetting = homePrefs.getString("temperature_unit", "metric")
        windSpeedSetting = homePrefs.getString("wind_speed_unit", "m/s")

        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.rvDailyForecast.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshWeatherData()
        }

        if (selectedLatitude != null && selectedLongitude != null) {
            fetchWeatherData(selectedLatitude!!, selectedLongitude!!)
            return
        }

        sharedLocationViewModel.selectedCoordinates.observe(viewLifecycleOwner, Observer { coordinates ->
            if (coordinates != null) {
                selectedLatitude = coordinates.first
                selectedLongitude = coordinates.second
                isFavoriteLocation = true
                (activity as? MainActivity)?.updateToolbarTitle("Favourite Location")
                fetchWeatherData(selectedLatitude!!, selectedLongitude!!)
            } else {
                selectedLatitude = homePrefs.getFloat("home_latitude", 0f)
                selectedLongitude = homePrefs.getFloat("home_longitude", 0f)
                isFavoriteLocation = false

                fetchWeatherData(selectedLatitude!!, selectedLongitude!!)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).getFab().visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
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
        selectedLatitude = homePrefs.getFloat("home_latitude", 0f)
        selectedLongitude = homePrefs.getFloat("home_longitude", 0f)
        isFavoriteLocation = false
        (activity as? MainActivity)?.updateToolbarTitle("Home")
        fetchWeatherData(selectedLatitude!!, selectedLongitude!!)
    }

    private fun fetchWeatherData(latitude: Float, longitude: Float) {
        viewModel.fetchWeatherData(
            latitude = latitude,
            longitude = longitude,
            apiKey = _apiKey,
            language = languageSetting ?: "en"
        )

        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weather ->
            weather?.let {
                updateWeatherUI(it)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        viewModel.forecastWeather.observe(viewLifecycleOwner, Observer { forecast ->
            forecast?.let {
                val currentTimeMillis = System.currentTimeMillis() / 1000
                val twentyFourHoursLater = currentTimeMillis + (24 * 60 * 60)
                val filteredHourlyForecastList = forecast.list.filter { weatherData ->
                    weatherData.unixTimeStamp in (currentTimeMillis + 1)..twentyFourHoursLater
                }

                if (filteredHourlyForecastList.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        R.string.no_hourly_forecast,
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                val hourlyAdapter = HourlyForecastAdapter(
                    forecast.city.timezone,
                    viewModel::formatHourlyTime,
                    temperatureSetting ?: "metric"
                )
                binding.rvHourlyForecast.adapter = hourlyAdapter
                hourlyAdapter.submitList(filteredHourlyForecastList)

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
                        val minTemp = entry.value.minOf { UnitConverter.convertTemperature(it.weatherDataOfHour.tempMin, "metric", temperatureSetting ?: "metric") }
                        val maxTemp = entry.value.maxOf { UnitConverter.convertTemperature(it.weatherDataOfHour.tempMax, "metric", temperatureSetting ?: "metric") }
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

                if (filteredDailyForecastList.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        R.string.no_daily_forecast,
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                val dailyAdapter = DailyForecastAdapter(
                    forecast.city.timezone,
                    languageSetting ?: "en",
                    temperatureSetting ?: "metric"
                )
                binding.rvDailyForecast.adapter = dailyAdapter
                dailyAdapter.submitList(filteredDailyForecastList)

                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

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
    }

    private fun updateWeatherUI(weather: CurrentWeatherResponse) {
        binding.tvCity.text = weather.currentLocationName
        val (date, time) = viewModel.formatDateTime(weather.unixTimeStamp, weather.offsetFromUTC)
        binding.tvDateTime.text = "$date • $time"
        val convertedTemp = UnitConverter.convertTemperature(weather.weatherData.temp.toDouble(), "metric", temperatureSetting ?: "metric")
        binding.tvTemperature.text = String.format("%.1f%s", convertedTemp, UnitConverter.getTemperatureUnitSymbol(temperatureSetting ?: "metric"))
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
        val convertedWindSpeed = UnitConverter.convertWindSpeed(weather.wind.speed, "m/s", windSpeedSetting ?: "m/s")
        binding.tvWindSpeed.text = String.format("%.1f %s", convertedWindSpeed, UnitConverter.getWindSpeedUnitSymbol(windSpeedSetting ?: "m/s"))
        binding.tvPressure.text = "${weather.weatherData.pressure} hPa"
        binding.tvClouds.text = "${weather.clouds.all}%"
    }

    private fun getWeatherRepository(): WeatherAppRepository {
        return (requireActivity() as MainActivity).weatherRepository
    }
}