package com.example.weatherapp.ui.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ItemHourlyForecastBinding
import com.example.weatherapp.model.pojos.WeatherData
import com.example.weatherapp.utils.UnitConverter

class HourlyForecastAdapter(
    private val timezone: Int,
    private val formatHourlyTime: (Long, Long) -> String,
    private val temperatureUnit: String
) : ListAdapter<WeatherData, HourlyForecastAdapter.HourlyForecastViewHolder>(WeatherDataDiffCallback()) {

    class HourlyForecastViewHolder(val binding: ItemHourlyForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        with(holder.binding) {
            tvHourlyTime.text = formatHourlyTime(forecast.unixTimeStamp, timezone.toLong())
            val temp = UnitConverter.convertTemperature(forecast.weatherDataOfHour.temp.toDouble(), "metric", temperatureUnit)
            tvHourlyTemperature.text = String.format("%.1f%s", temp, UnitConverter.getTemperatureUnitSymbol(temperatureUnit))
            tvHourlyDescription.text = forecast.weather.firstOrNull()?.weatherDescriptionOfHour?.replaceFirstChar {
                it.uppercaseChar()
            } ?: ""
            forecast.weather.firstOrNull()?.icon?.let { iconCode ->
                val resourceName = "ic_$iconCode"
                val resourceId = root.context.resources.getIdentifier(
                    resourceName,
                    "drawable",
                    root.context.packageName
                ) ?: 0
                if (resourceId != 0) {
                    Glide.with(root.context)
                        .load(resourceId)
                        .into(ivHourlyWeatherIcon)
                } else {
                    Glide.with(root.context)
                        .load(R.drawable.ic_launcher_background)
                        .into(ivHourlyWeatherIcon)
                }
            }
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