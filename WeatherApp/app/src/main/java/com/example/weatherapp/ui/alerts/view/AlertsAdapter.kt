package com.example.weatherapp.ui.alerts.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemAlertBinding
import com.example.weatherapp.model.pojos.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertsAdapter(
    private val onDeleteClick: (Alert) -> Unit
) : ListAdapter<Alert, AlertsAdapter.AlarmViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert) = oldItem.alertId == newItem.alertId
        override fun areContentsTheSame(oldItem: Alert, newItem: Alert) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlarmViewHolder(
        private val binding: ItemAlertBinding,
        private val onDeleteClick: (Alert) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: Alert) {
            binding.apply {
                textViewDate.text = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(alert.dateMillis))
                textViewFromTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alert.alertTriggerAt))
                textViewToTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alert.alertStopAt))
                alertType.text = when (alert.isAlarm) {
                    true -> "Alarm"
                    false -> "Notification"
                }
                buttonDelete.setOnClickListener {
                    onDeleteClick(alert)
                }
            }
        }
    }
}