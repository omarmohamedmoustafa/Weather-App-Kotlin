package com.example.weatherapp.ui.home.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ItemDailyForecastBinding
import com.example.weatherapp.model.pojos.WeatherData
import com.example.weatherapp.utils.UnitConverter
import java.text.SimpleDateFormat
import java.util.*

class DailyForecastAdapter(
    private val timezone: Int,
    private val language: String,
    private val temperatureUnit: String
) : ListAdapter<WeatherData, DailyForecastAdapter.DailyForecastViewHolder>(WeatherDataDiffCallback()) {

    class DailyForecastViewHolder(val binding: ItemDailyForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding = ItemDailyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        with(holder.binding) {
            // Format the date with the appropriate locale
            val locale = Locale(language)
            val dateFormat = SimpleDateFormat("EEEE", locale)
            val timeZone = TimeZone.getTimeZone("GMT")
            timeZone.rawOffset = timezone * 1000
            dateFormat.timeZone = timeZone
            val date = Date(forecast.unixTimeStamp * 1000)
            val formattedDay = dateFormat.format(date)

            tvDailyDay.text = formattedDay

            // Format temperature based on unit
            val minTemp = UnitConverter.convertTemperature(forecast.weatherDataOfHour.tempMin, "metric", temperatureUnit)
            val maxTemp = UnitConverter.convertTemperature(forecast.weatherDataOfHour.tempMax, "metric", temperatureUnit)
            tvDailyTemperature.text = String.format(
                "%.1f%s/%.1f%s",
                minTemp,
                UnitConverter.getTemperatureUnitSymbol(temperatureUnit),
                maxTemp,
                UnitConverter.getTemperatureUnitSymbol(temperatureUnit)
            )

            // Set weather icon
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
                        .into(ivDailyWeatherIcon)
                } else {
                    Glide.with(root.context)
                        .load(R.drawable.ic_launcher_background)
                        .into(ivDailyWeatherIcon)
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