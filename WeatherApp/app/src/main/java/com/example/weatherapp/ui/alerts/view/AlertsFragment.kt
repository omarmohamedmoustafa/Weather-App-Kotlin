package com.example.weatherapp.ui.alerts.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.activities.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentAlertsBinding
import com.example.weatherapp.model.alert.AlertsHelper
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.alert.AlertReceiver
import com.example.weatherapp.ui.alerts.view_model.AlertsViewModel
import com.example.weatherapp.ui.alerts.view_model.AlertsViewModelFactory
import java.util.*

class AlertsFragment : Fragment() {

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).getFab().visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).getFab().visibility = View.GONE
    }

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var alertsHelper: AlertsHelper
    private val viewModel: AlertsViewModel by viewModels {
        AlertsViewModelFactory(
            (requireActivity() as MainActivity).weatherRepository,
            alertsHelper
        )
    }
    private lateinit var selectedDate: Calendar
    private lateinit var alertsAdapter: AlertsAdapter
    private lateinit var alarmDismissReceiver: BroadcastReceiver

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                requireContext(),
                "Notification permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertsHelper = AlertsHelper(requireContext())

        // Initialize and register broadcast receiver
        alarmDismissReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                viewModel.getAllAlarms()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            alarmDismissReceiver,
            IntentFilter(AlertReceiver.Companion.ACTION_ALARM_DISMISSED)
        )

        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setupRecyclerView()
        val fab = (requireActivity() as MainActivity).getFab()
        fab.setOnClickListener {
            showDatePickerDialog()
        }
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            alertsAdapter.submitList(alarms.toList())
        }
    }

    private fun setupRecyclerView() {
        alertsAdapter = AlertsAdapter(
            onDeleteClick = { alert ->
            AlertDialog.Builder(this.requireContext())
                .setTitle("Delete Alert")
                .setMessage("Are you sure you want to delete alert?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteAlarm(alert)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
            }
        )
        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = alertsAdapter
        }
    }

    private fun showDatePickerDialog() {
        selectedDate = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                showTimePickerDialog()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.date_time_picker)

        val fromTimePicker = dialog.findViewById<TimePicker>(R.id.fromTimePicker)
        val toTimePicker = dialog.findViewById<TimePicker>(R.id.toTimePicker)
        val alarmSwitch = dialog.findViewById<Switch>(R.id.alarmSwitch)
        val notificationSwitch = dialog.findViewById<Switch>(R.id.notificationSwitch)
        val confirmButton = dialog.findViewById<View>(R.id.confirmButton)

        val calendar = Calendar.getInstance()
        fromTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        fromTimePicker.minute = calendar.get(Calendar.MINUTE)
        toTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        toTimePicker.minute = calendar.get(Calendar.MINUTE)

        confirmButton.setOnClickListener {
            val fromTimeCal = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, fromTimePicker.hour)
                set(Calendar.MINUTE, fromTimePicker.minute)
            }

            val toTimeCal = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, toTimePicker.hour)
                set(Calendar.MINUTE, toTimePicker.minute)
            }

            // Validate time range
            if (toTimeCal.timeInMillis <= fromTimeCal.timeInMillis) {
                Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Prevent both switches from being off or both on
            if (!alarmSwitch.isChecked && !notificationSwitch.isChecked) {
                Toast.makeText(context, "At least one switch must be enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (alarmSwitch.isChecked && notificationSwitch.isChecked) {
                Toast.makeText(context, "Alarm and Notification cannot both be enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addAlarm(
                Alert(
                    dateMillis = selectedDate.timeInMillis,
                    alertTriggerAt = fromTimeCal.timeInMillis,
                    alertStopAt = toTimeCal.timeInMillis,
                    isAlarm = alarmSwitch.isChecked
                )
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(alarmDismissReceiver)
        super.onDestroyView()
        _binding = null
    }
}