package com.example.weatherapp.ui.settings.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.activities.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.model.location.LocationHelper
import com.example.weatherapp.ui.settings.view_model.SettingsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var locationHelper: LocationHelper
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    )   { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted && coarseLocationGranted) {
            fetchGpsLocation()
        } else {
            binding.gps.isChecked = false
            binding.map.isChecked = true
            viewModel.saveLocationPreference(requireContext(), false, true)
            Snackbar.make(
                binding.root,
                "Location permissions denied, defaulting to map",
                Snackbar.LENGTH_LONG
            ).setAction("Settings") {
                openAppSettings()
            }.show()
        }
    }
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.saveNotificationPreference(requireContext(), true)
            Snackbar.make(binding.root, "Notifications enabled", Snackbar.LENGTH_SHORT).show()
        } else {
            binding.notificationsDisabled.isChecked = true
            binding.notificationsEnabled.isChecked = false
            viewModel.saveNotificationPreference(requireContext(), false)
            Snackbar.make(binding.root, "Notification permission denied", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationHelper = (requireActivity() as MainActivity).locationHelper
        initializeUI()
        setupRadioGroups()
        observeGpsLocationResult()
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            try {
                val latitude = bundle.getDouble("lat")
                val longitude = bundle.getDouble("lon")
                viewModel.saveMapLocation(
                    requireContext(),
                    latitude.toFloat(),
                    longitude.toFloat(),
                )
                binding.map.isChecked = true
                binding.gps.isChecked = false
                Toast.makeText(requireContext(), "Location saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.gps.isChecked = true
                binding.map.isChecked = false
                viewModel.saveLocationPreference(requireContext(), true, false)
                Toast.makeText(requireContext(), "Failed to select location, defaulting to GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeUI() {
        if (viewModel.getLocationPreference(requireContext())) {
            binding.gps.isChecked = true
            binding.map.isChecked = false
        } else if (viewModel.getMapPreference(requireContext())) {
            binding.map.isChecked = true
            binding.gps.isChecked = false
        }

        if (viewModel.getNotificationPreference(requireContext())) {
            binding.notificationsEnabled.isChecked = true
            binding.notificationsDisabled.isChecked = false
        } else {
            binding.notificationsDisabled.isChecked = true
            binding.notificationsEnabled.isChecked = false
        }

        when (viewModel.getTemperatureUnit(requireContext())) {
            "metric" -> binding.radioCelsius.isChecked = true
            "imperial" -> binding.radioFahrenheit.isChecked = true
            "standard" -> binding.radioKelvin.isChecked = true
            else -> binding.radioCelsius.isChecked = true
        }

        // Initialize wind speed radio buttons and set enabled state based on temperature
        val isFahrenheit = viewModel.getTemperatureUnit(requireContext()) == "imperial"
        binding.radioMilesPerHour.isEnabled = isFahrenheit
        binding.radioMetersPerSecond.isEnabled = !isFahrenheit
        when (viewModel.getWindSpeedUnit(requireContext())) {
            "m/s" -> if (!isFahrenheit) binding.radioMetersPerSecond.isChecked = true else binding.radioMilesPerHour.isChecked = true
            "mph" -> if (isFahrenheit) binding.radioMilesPerHour.isChecked = true else binding.radioMetersPerSecond.isChecked = true
            else -> if (isFahrenheit) binding.radioMilesPerHour.isChecked = true else binding.radioMetersPerSecond.isChecked = true
        }

        when (viewModel.getLanguage(requireContext())) {
            "en" -> binding.radioEnglish.isChecked = true
            "ar" -> binding.radioArabic.isChecked = true
            else -> binding.radioEnglish.isChecked = true
        }
    }

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    private fun setupRadioGroups() {
        binding.locationRadioGroup.setOnCheckedChangeListener  { _, checkedId ->
            when (checkedId) {
                binding.gps.id -> {
                    checkAndRequestLocationPermissions()
                }
                binding.map.id -> {
                    viewModel.saveLocationPreference(requireContext(), false, true)
                    findNavController().navigate(R.id.action_settingsFragment_to_mapFragment)
                }
            }
        }

        binding.notificationsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.notificationsEnabled.id -> {
                    viewModel.saveNotificationPreference(requireContext(), true)
                    // Request notification permission if enabled
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // For older versions, just save preference
                        viewModel.saveNotificationPreference(requireContext(), true)
                    }
                }
                binding.notificationsDisabled.id -> {
                    viewModel.saveNotificationPreference(requireContext(), false)
                    Snackbar.make(
                        binding.root,
                        "You can manage permissions in app settings.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        openAppSettings()
                    }.show()
                }
            }
        }

        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioCelsius.id -> {
                    viewModel.saveTemperatureUnit(requireContext(), "metric")
                    binding.radioMilesPerHour.isEnabled = false
                    binding.radioMetersPerSecond.isEnabled = true
                    if (viewModel.getWindSpeedUnit(requireContext()) != "m/s") {
                        viewModel.saveWindSpeedUnit(requireContext(), "m/s")
                        binding.radioMetersPerSecond.isChecked = true
                        Snackbar.make(
                            binding.root,
                            "Wind speed set to m/s as Celsius is selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                binding.radioFahrenheit.id -> {
                    viewModel.saveTemperatureUnit(requireContext(), "imperial")
                    binding.radioMilesPerHour.isEnabled = true
                    binding.radioMetersPerSecond.isEnabled = false
                    if (viewModel.getWindSpeedUnit(requireContext()) != "mph") {
                        viewModel.saveWindSpeedUnit(requireContext(), "mph")
                        binding.radioMilesPerHour.isChecked = true
                        Snackbar.make(
                            binding.root,
                            "Wind speed set to mph as Fahrenheit is selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                binding.radioKelvin.id -> {
                    viewModel.saveTemperatureUnit(requireContext(), "standard")
                    binding.radioMilesPerHour.isEnabled = false
                    binding.radioMetersPerSecond.isEnabled = true
                    if (viewModel.getWindSpeedUnit(requireContext()) != "m/s") {
                        viewModel.saveWindSpeedUnit(requireContext(), "m/s")
                        binding.radioMetersPerSecond.isChecked = true
                        Snackbar.make(
                            binding.root,
                            "Wind speed set to m/s as Kelvin is selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioMetersPerSecond.id -> {
                    if (viewModel.getTemperatureUnit(requireContext()) == "imperial") {
                        binding.radioMilesPerHour.isChecked = true
                        viewModel.saveWindSpeedUnit(requireContext(), "mph")
                        Snackbar.make(
                            binding.root,
                            "Meters per second is not available with Fahrenheit",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.saveWindSpeedUnit(requireContext(), "m/s")
                    }
                }
                binding.radioMilesPerHour.id -> {
                    if (viewModel.getTemperatureUnit(requireContext()) != "imperial") {
                        binding.radioMetersPerSecond.isChecked = true
                        viewModel.saveWindSpeedUnit(requireContext(), "m/s")
                        Snackbar.make(
                            binding.root,
                            "Miles per hour is only available with Fahrenheit",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.saveWindSpeedUnit(requireContext(), "mph")
                    }
                }
            }
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioEnglish.id -> {
                    viewModel.saveLanguage(requireContext(), "en")
                    (requireActivity() as MainActivity).updateLocale("en")
                    (requireActivity() as MainActivity).restartActivity(findNavController().currentDestination?.id ?: R.id.nav_settings)
                }
                binding.radioArabic.id -> {
                    viewModel.saveLanguage(requireContext(), "ar")
                    (requireActivity() as MainActivity).updateLocale("ar")
                    (requireActivity() as MainActivity).restartActivity(findNavController().currentDestination?.id ?: R.id.nav_settings)
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun checkAndRequestLocationPermissions() {
        when {
            locationHelper.hasLocationPermissions() -> {
                fetchGpsLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Snackbar.make(
                    binding.root,
                    "Location permissions are required for GPS",
                    Snackbar.LENGTH_LONG
                ).setAction("Grant") {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }.show()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchGpsLocation() {
        viewModel.saveGpsLocation(requireContext(), locationHelper,
            CoroutineScope(Dispatchers.Main)
        )
    }

    private fun observeGpsLocationResult() {
        viewModel.getGpsLocationResult().observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "GPS location saved successfully", Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                binding.gps.isChecked = false
                binding.map.isChecked = true
                viewModel.saveLocationPreference(requireContext(), false, true)
                Snackbar.make(
                    binding.root,
                    "Failed to retrieve GPS location: ${exception.message}",
                    Snackbar.LENGTH_LONG
                ).setAction("Settings") {
                    openAppSettings()
                }.show()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}