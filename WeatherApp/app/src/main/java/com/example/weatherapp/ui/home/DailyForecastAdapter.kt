package com.example.weatherapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.pojos.WeatherData
import java.text.SimpleDateFormat
import java.util.*

class DailyForecastAdapter(
    private val forecastList: List<WeatherData>,
    private val timezone: Int
) : RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder>() {

    class DailyForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDailyDay)
        val tvTemperature: TextView = itemView.findViewById(R.id.tvDailyTemperature)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_five_day_forecast, parent, false)
        return DailyForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        val forecast = forecastList[position]
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val timeZone = TimeZone.getTimeZone("GMT")
        timeZone.rawOffset = timezone * 1000
        dateFormat.timeZone = timeZone
        val date = Date(forecast.unixTimeStamp * 1000)
        val formattedDay = dateFormat.format(date)
        holder.tvDay.text = formattedDay
        holder.tvTemperature.text = String.format("%.1f°C/%.1f°C", forecast.weatherDataOfHour.tempMin, forecast.weatherDataOfHour.tempMax)
    }

    override fun getItemCount(): Int = forecastList.size
}