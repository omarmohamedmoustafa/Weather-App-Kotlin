package com.example.weatherapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel
    private var pendingChanges = false
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var isProgrammaticChange = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        viewModel.initialize(requireContext())

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRadioButtons()
        setupFragmentResultListener()

        viewModel.location.observe(viewLifecycleOwner) { location ->
            isProgrammaticChange = true
            setRadioButton(binding.rgLocation, location)
            isProgrammaticChange = false
        }
        viewModel.language.observe(viewLifecycleOwner) { language ->
            isProgrammaticChange = true
            setRadioButton(binding.rgLanguage, language)
            isProgrammaticChange = false
        }
        viewModel.temperature.observe(viewLifecycleOwner) { temperature ->
            isProgrammaticChange = true
            setRadioButton(binding.rgTemperature, temperature)
            isProgrammaticChange = false
        }
        viewModel.windSpeed.observe(viewLifecycleOwner) { windSpeed ->
            isProgrammaticChange = true
            setRadioButton(binding.rgWindSpeed, windSpeed)
            isProgrammaticChange = false
        }
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            isProgrammaticChange = true
            setRadioButton(binding.rgNotifications, notifications)
            isProgrammaticChange = false
        }

        return root
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            selectedLatitude = bundle.getDouble("lat")
            selectedLongitude = bundle.getDouble("lon")
            pendingChanges = true
            viewModel.saveSettings(
                location = "Map",
                language = binding.rgLanguage.findViewById<RadioButton>(binding.rgLanguage.checkedRadioButtonId)?.text?.toString() ?: viewModel.language.value ?: "English",
                temperature = binding.rgTemperature.findViewById<RadioButton>(binding.rgTemperature.checkedRadioButtonId)?.text?.toString() ?: viewModel.temperature.value ?: "Celsius",
                windSpeed = binding.rgWindSpeed.findViewById<RadioButton>(binding.rgWindSpeed.checkedRadioButtonId)?.text?.toString() ?: viewModel.windSpeed.value ?: "Meter/Sec",
                notifications = binding.rgNotifications.findViewById<RadioButton>(binding.rgNotifications.checkedRadioButtonId)?.text?.toString() ?: viewModel.notifications.value ?: "Enable",
                latitude = selectedLatitude,
                longitude = selectedLongitude
            )
        }
    }

    private fun setupRadioButtons() {
        binding.rgLocation.setOnCheckedChangeListener { _, checkedId ->
            if (!isProgrammaticChange) {
                val selected = binding.rgLocation.findViewById<RadioButton>(checkedId)?.text?.toString() ?: return@setOnCheckedChangeListener
                if (selected == "Map") {
                    findNavController().navigate(R.id.action_settingsFragment_to_mapFragment)
                } else {
                    pendingChanges = true
                    viewModel.saveSettings(
                        location = selected,
                        language = binding.rgLanguage.findViewById<RadioButton>(binding.rgLanguage.checkedRadioButtonId)?.text?.toString() ?: viewModel.language.value ?: "English",
                        temperature = binding.rgTemperature.findViewById<RadioButton>(binding.rgTemperature.checkedRadioButtonId)?.text?.toString() ?: viewModel.temperature.value ?: "Celsius",
                        windSpeed = binding.rgWindSpeed.findViewById<RadioButton>(binding.rgWindSpeed.checkedRadioButtonId)?.text?.toString() ?: viewModel.windSpeed.value ?: "Meter/Sec",
                        notifications = binding.rgNotifications.findViewById<RadioButton>(binding.rgNotifications.checkedRadioButtonId)?.text?.toString() ?: viewModel.notifications.value ?: "Enable",
                        latitude = selectedLatitude,
                        longitude = selectedLongitude
                    )
                }
            }
        }
        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            if (!isProgrammaticChange) {
                val selected = binding.rgLanguage.findViewById<RadioButton>(checkedId)?.text?.toString() ?: return@setOnCheckedChangeListener
                pendingChanges = true
                viewModel.saveSettings(
                    location = binding.rgLocation.findViewById<RadioButton>(binding.rgLocation.checkedRadioButtonId)?.text?.toString() ?: viewModel.location.value ?: "GPS",
                    language = selected,
                    temperature = binding.rgTemperature.findViewById<RadioButton>(binding.rgTemperature.checkedRadioButtonId)?.text?.toString() ?: viewModel.temperature.value ?: "Celsius",
                    windSpeed = binding.rgWindSpeed.findViewById<RadioButton>(binding.rgWindSpeed.checkedRadioButtonId)?.text?.toString() ?: viewModel.windSpeed.value ?: "Meter/Sec",
                    notifications = binding.rgNotifications.findViewById<RadioButton>(binding.rgNotifications.checkedRadioButtonId)?.text?.toString() ?: viewModel.notifications.value ?: "Enable",
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )
            }
        }
        binding.rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            if (!isProgrammaticChange) {
                val selected = binding.rgTemperature.findViewById<RadioButton>(checkedId)?.text?.toString() ?: return@setOnCheckedChangeListener
                pendingChanges = true
                viewModel.saveSettings(
                    location = binding.rgLocation.findViewById<RadioButton>(binding.rgLocation.checkedRadioButtonId)?.text?.toString() ?: viewModel.location.value ?: "GPS",
                    language = binding.rgLanguage.findViewById<RadioButton>(binding.rgLanguage.checkedRadioButtonId)?.text?.toString() ?: viewModel.language.value ?: "English",
                    temperature = selected,
                    windSpeed = binding.rgWindSpeed.findViewById<RadioButton>(binding.rgWindSpeed.checkedRadioButtonId)?.text?.toString() ?: viewModel.windSpeed.value ?: "Meter/Sec",
                    notifications = binding.rgNotifications.findViewById<RadioButton>(binding.rgNotifications.checkedRadioButtonId)?.text?.toString() ?: viewModel.notifications.value ?: "Enable",
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )
            }
        }
        binding.rgWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            if (!isProgrammaticChange) {
                val selected = binding.rgWindSpeed.findViewById<RadioButton>(checkedId)?.text?.toString() ?: return@setOnCheckedChangeListener
                pendingChanges = true
                viewModel.saveSettings(
                    location = binding.rgLocation.findViewById<RadioButton>(binding.rgLocation.checkedRadioButtonId)?.text?.toString() ?: viewModel.location.value ?: "GPS",
                    language = binding.rgLanguage.findViewById<RadioButton>(binding.rgLanguage.checkedRadioButtonId)?.text?.toString() ?: viewModel.language.value ?: "English",
                    temperature = binding.rgTemperature.findViewById<RadioButton>(binding.rgTemperature.checkedRadioButtonId)?.text?.toString() ?: viewModel.temperature.value ?: "Celsius",
                    windSpeed = selected,
                    notifications = binding.rgNotifications.findViewById<RadioButton>(binding.rgNotifications.checkedRadioButtonId)?.text?.toString() ?: viewModel.notifications.value ?: "Enable",
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )
            }
        }
        binding.rgNotifications.setOnCheckedChangeListener { _, checkedId ->
            if (!isProgrammaticChange) {
                val selected = binding.rgNotifications.findViewById<RadioButton>(checkedId)?.text?.toString() ?: return@setOnCheckedChangeListener
                pendingChanges = true
                viewModel.saveSettings(
                    location = binding.rgLocation.findViewById<RadioButton>(binding.rgLocation.checkedRadioButtonId)?.text?.toString() ?: viewModel.location.value ?: "GPS",
                    language = binding.rgLanguage.findViewById<RadioButton>(binding.rgLanguage.checkedRadioButtonId)?.text?.toString() ?: viewModel.language.value ?: "English",
                    temperature = binding.rgTemperature.findViewById<RadioButton>(binding.rgTemperature.checkedRadioButtonId)?.text?.toString() ?: viewModel.temperature.value ?: "Celsius",
                    windSpeed = binding.rgWindSpeed.findViewById<RadioButton>(binding.rgWindSpeed.checkedRadioButtonId)?.text?.toString() ?: viewModel.windSpeed.value ?: "Meter/Sec",
                    notifications = selected,
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )
            }
        }
    }

    private fun setRadioButton(radioGroup: RadioGroup, value: String) {
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            if (radioButton.text == value) {
                radioButton.isChecked = true
                break
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (pendingChanges) {
            viewModel.saveSettings(
                location = binding.rgLocation.findViewById<RadioButton>(binding.rgLocation.checkedRadioButtonId)?.text?.toString() ?: viewModel.location.value ?: "GPS",
                language = binding.rgLanguage.findViewById<RadioButton>(binding.rgLanguage.checkedRadioButtonId)?.text?.toString() ?: viewModel.language.value ?: "English",
                temperature = binding.rgTemperature.findViewById<RadioButton>(binding.rgTemperature.checkedRadioButtonId)?.text?.toString() ?: viewModel.temperature.value ?: "Celsius",
                windSpeed = binding.rgWindSpeed.findViewById<RadioButton>(binding.rgWindSpeed.checkedRadioButtonId)?.text?.toString() ?: viewModel.windSpeed.value ?: "Meter/Sec",
                notifications = binding.rgNotifications.findViewById<RadioButton>(binding.rgNotifications.checkedRadioButtonId)?.text?.toString() ?: viewModel.notifications.value ?: "Enable",
                latitude = selectedLatitude,
                longitude = selectedLongitude
            )
            pendingChanges = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}