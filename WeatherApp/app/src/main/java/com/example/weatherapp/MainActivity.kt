package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.example.mvvm.model.repository.WeatherRepository
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.local.LocalDataSource
import com.example.weatherapp.model.local.WeatherDao
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.remote.RemoteDataSource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var weatherDao: WeatherDao
    private lateinit var localDataSource: LocalDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_settings, R.id.nav_favourites, R.id.nav_alerts),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize dependencies
        weatherRepository = WeatherRepository(RemoteDataSource(), LocalDataSource(WeatherDatabase.getDatabase(this).weatherDao()))

        // Test weather API and Room storage
        testWeatherApi()
    }

    private fun testWeatherApi() {
        lifecycleScope.launch {
            val latitude = 30.06464f
            val longitude = 31.2705024f
            val apiKey = "7dcce3c6b65dedcdd3bd946e7b4c20f2"
            val units = "metric"
            val language = "en"

            // Fetch weather data
            Log.d("MainActivity", "Fetching weather data...")
            val result = weatherRepository.getWeatherForecast(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )

            // Handle result
            result.fold(
                onSuccess = { weatherResponse ->
                    Log.d("MainActivity", "Weather data fetched: ${weatherResponse.city.name}, ${weatherResponse.list.size} forecasts")
                    // Verify Room storage
                    val storedData = localDataSource.getWeatherResponse()
                    if (storedData != null) {
                        Log.d("MainActivity", "Data verified in Room: ${storedData.city.name}, ${storedData.list.size} forecasts")
                        Snackbar.make(
                            binding.root,
                            "Weather data stored: ${storedData.city.name}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Log.e("MainActivity", "No data found in Room")
                        Snackbar.make(
                            binding.root,
                            "Failed to verify data in Room",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                },
                onFailure = { exception ->
                    Log.e("MainActivity", "Failed to fetch weather data: ${exception.message}")
                    Snackbar.make(
                        binding.root,
                        "Error: ${exception.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    // Check Room for cached data
                    val storedData = localDataSource.getWeatherResponse()
                    if (storedData != null) {
                        Log.d("MainActivity", "Cached data found: ${storedData.city.name}")
                        Snackbar.make(
                            binding.root,
                            "Using cached data: ${storedData.city.name}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}