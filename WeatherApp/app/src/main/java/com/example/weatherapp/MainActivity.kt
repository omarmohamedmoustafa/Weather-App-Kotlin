package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.weatherapp.model.local.fav_local.FavCountriesDatabase
import com.example.weatherapp.model.local.weather_local.WeatherDatabase
import com.example.weatherapp.model.location.LocationHelper
import com.example.weatherapp.model.remote.RemoteDataSource
import com.example.weatherapp.model.repository.WeatherAppRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import android.preference.PreferenceManager
import androidx.annotation.RequiresPermission

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var weatherRepository: WeatherAppRepository
    lateinit var locationHelper: LocationHelper
    private val LOCATION_PERMISSION_CODE = 1
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize WeatherDao and dependencies
        weatherRepository = WeatherAppRepository.getInstance(
            remoteDataSource = RemoteDataSource(),
            localDataSource = LocalDataSource(
                WeatherDatabase.getDatabase(this).weatherDao(),
                FavCountriesDatabase.getDatabase(this).favDao()
            )
        )

        // Initialize LocationHelper
        locationHelper = LocationHelper(this) { latitude, longitude, _ ->
            Log.d("MainActivity", "Location updated: Lat=$latitude, Lon=$longitude")
        }

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

    // Expose FAB for fragments to control
    fun getFab(): FloatingActionButton {
        return binding.appBarMain.fab
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            locationHelper.startLocationUpdates()
        } else {
            Snackbar.make(
                binding.root,
                "Location permissions denied. Some features may not work.",
                Snackbar.LENGTH_LONG
            ).show()
        }
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
}