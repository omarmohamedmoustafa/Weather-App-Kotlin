package com.example.weatherapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.model.pojos.WeatherData

class HourlyForecastAdapter(
    private val forecastList: List<WeatherData>,
    private val timezone: Int,
    private val formatHourlyTime: (Long, Long) -> String
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>() {

    class HourlyForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvHourlyTime)
        val tvTemperature: TextView = itemView.findViewById(R.id.tvHourlyTemperature)
        val ivWeatherIcon: ImageView = itemView.findViewById(R.id.ivHourlyWeatherIcon)
        val tvDescription: TextView = itemView.findViewById(R.id.tvHourlyDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val forecast = forecastList[position]
        holder.tvTime.text = formatHourlyTime(forecast.unixTimeStamp, timezone.toLong())
        holder.tvTemperature.text = String.format("%.1f°C", forecast.weatherDataOfHour.temp)
        holder.tvDescription.text = forecast.weather.firstOrNull()?.weatherDescriptionOfHour?.replaceFirstChar {
            it.uppercaseChar()
        } ?: ""
        forecast.weather.firstOrNull()?.icon?.let { iconCode ->
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            Glide.with(holder.itemView.context)
                .load(iconUrl)
                .into(holder.ivWeatherIcon)
        }
    }

    override fun getItemCount(): Int = forecastList.size
}