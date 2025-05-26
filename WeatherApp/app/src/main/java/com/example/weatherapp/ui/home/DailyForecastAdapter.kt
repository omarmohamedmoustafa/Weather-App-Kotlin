package com.example.weatherapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemFiveDayForecastBinding
import com.example.weatherapp.model.pojos.WeatherData
import java.text.SimpleDateFormat
import java.util.*

class DailyForecastAdapter(
    private val timezone: Int
) : ListAdapter<WeatherData, DailyForecastAdapter.DailyForecastViewHolder>(WeatherDataDiffCallback()) {

    class DailyForecastViewHolder(val binding: ItemFiveDayForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding = ItemFiveDayForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        with(holder.binding) {
            val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val timeZone = TimeZone.getTimeZone("GMT")
            timeZone.rawOffset = timezone * 1000
            dateFormat.timeZone = timeZone
            val date = Date(forecast.unixTimeStamp * 1000)
            val formattedDay = dateFormat.format(date)
            tvDailyDay.text = formattedDay
            tvDailyTemperature.text = String.format("%.1f°C/%.1f°C", forecast.weatherDataOfHour.tempMin, forecast.weatherDataOfHour.tempMax)
        }
    }

    class WeatherDataDiffCallback : DiffUtil.ItemCallback<WeatherData>() {
        override fun areItemsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem.unixTimeStamp == newItem.unixTimeStamp
        }

        override fun areContentsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem == newItem
        }
    }
}