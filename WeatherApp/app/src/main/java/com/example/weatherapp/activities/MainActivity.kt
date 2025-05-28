package com.example.weatherapp.activities

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.local.LocalDataSource
import com.example.weatherapp.model.local.WeatherAppDatabase
import com.example.weatherapp.model.location.LocationHelper
import com.example.weatherapp.model.remote.RemoteDataSource
import com.example.weatherapp.model.repository.WeatherAppRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import com.example.weatherapp.R
import com.example.weatherapp.model.alert.AlertsHelper
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var weatherRepository: WeatherAppRepository
    lateinit var locationHelper: LocationHelper
    lateinit var alertsHelper : AlertsHelper
    private val LOCATION_PERMISSION_CODE = 1
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        // Initialize WeatherDao and dependencies
        weatherRepository = WeatherAppRepository.getInstance(
            remoteDataSource = RemoteDataSource(),
            localDataSource = LocalDataSource(
                WeatherAppDatabase.getDatabase(this).weatherDao()
            )
        )

        // Initialize LocationHelper
        locationHelper = LocationHelper(this) { latitude, longitude, _ ->
            Log.d("MainActivity", "Location updated: Lat=$latitude, Lon=$longitude")
        }
        alertsHelper = AlertsHelper(this)

        // Inflate the layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Initialize FAB as hidden by default
        binding.appBarMain.fab.visibility = GONE

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_settings, R.id.nav_favourites, R.id.nav_alerts),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set up the navigation graph with the default start destination
        navController.setGraph(R.navigation.mobile_navigation)

        // Check SharedPreferences for saved location and navigate accordingly
        val sharedPreferences = getSharedPreferences("HomeSettingsPrefs", MODE_PRIVATE)
        val latitude = sharedPreferences.getFloat("home_latitude", 0f)
        val longitude = sharedPreferences.getFloat("home_longitude", 0f)

        if (latitude != 0f && longitude != 0f) {
            navController.navigate(R.id.action_initialSetup_to_nav_home)
        }

        // Check and request location permissions (still needed for other fragments like MapFragment)
        if (!locationHelper.hasLocationPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    // Expose FAB for fragments to control it's visibility
    fun getFab(): FloatingActionButton {
        return binding.appBarMain.fab
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
    }
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "alerts_channel",
            "Weather Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for weather alert notifications"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun restartActivity(currentDestinationId: Int = R.id.nav_settings) {
        val intent = intent
        intent.putExtra("destinationId", currentDestinationId)
        finish()
        startActivity(intent)
    }
    fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }
}