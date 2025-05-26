package com.example.weatherapp.ui.setup

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentInitialSetupBinding
import com.example.weatherapp.model.location.LocationHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InitialSetupFragment : Fragment() {
    private var _binding: FragmentInitialSetupBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationHelper: LocationHelper
    private lateinit var sharedPreferences: SharedPreferences

    // Register permission launcher for notifications
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        ]
    )
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, save notification preference
            savePreferences(notificationsEnabled = true)
            Snackbar.make(binding.root, "Notifications enabled", Snackbar.LENGTH_SHORT).show()
        } else {
            // Permission denied, uncheck the checkbox and save preference
            binding.cbNotifications.isChecked = false
            savePreferences(notificationsEnabled = false)
            Snackbar.make(binding.root, "Notification permission denied", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitialSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = (requireActivity() as MainActivity).locationHelper
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Initialize checkbox state from SharedPreferences
        binding.cbNotifications.isChecked = sharedPreferences.getBoolean("notifications", false)

        // Set up radio buttons
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_gps -> {
                    // Enable GPS selection
                    binding.radioMap.isEnabled = false
                }
                R.id.radio_map -> {
                    // Enable map selection
                    binding.radioGps.isEnabled = false
                }
            }
        }

        // Set up notification checkbox
        binding.cbNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAndRequestNotificationPermission()
            } else {
                // Save preference as false if unchecked
                savePreferences(notificationsEnabled = false)
            }
        }

        // OK button click
        binding.btnOk.setOnClickListener {
            when (binding.radioGroup.checkedRadioButtonId) {
                R.id.radio_gps -> {
                    if (locationHelper.hasLocationPermissions()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val (lat, lon, address) = withContext(Dispatchers.IO) {
                                    locationHelper.getLastKnownLocation()
                                }
                                if (lat != null && lon != null) {
                                    savePreferences(lat, lon, binding.cbNotifications.isChecked)
                                    navigateToHome()
                                } else {
                                    Snackbar.make(binding.root, "Unable to retrieve location", Snackbar.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Snackbar.make(binding.root, "Error retrieving location: ${e.message}", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Snackbar.make(binding.root, "Location permission required", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Settings") {
                                openAppSettings()
                            }
                            .show()
                    }
                }
                R.id.radio_map -> {
                    findNavController().navigate(R.id.action_initialSetup_to_mapFragment)
                }
                else -> {
                    Snackbar.make(binding.root, "Please select a location method", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // Listen for map fragment result
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            val lat = bundle.getDouble("lat")
            val lon = bundle.getDouble("lon")
            savePreferences(lat, lon, binding.cbNotifications.isChecked)
            navigateToHome()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, save preference
                savePreferences(notificationsEnabled = true)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale if needed
                Snackbar.make(
                    binding.root,
                    "Notification permission is required to receive weather updates",
                    Snackbar.LENGTH_LONG
                ).setAction("Grant") {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }.show()
            }
            else -> {
                // Request permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun savePreferences(
        latitude: Double = sharedPreferences.getFloat("home_latitude", 0f).toDouble(),
        longitude: Double = sharedPreferences.getFloat("home_longitude", 0f).toDouble(),
        notificationsEnabled: Boolean
    ) {
        sharedPreferences.edit {
            putFloat("home_latitude", latitude.toFloat())
            putFloat("home_longitude", longitude.toFloat())
            putBoolean("notifications", notificationsEnabled)
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_initialSetup_to_nav_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
