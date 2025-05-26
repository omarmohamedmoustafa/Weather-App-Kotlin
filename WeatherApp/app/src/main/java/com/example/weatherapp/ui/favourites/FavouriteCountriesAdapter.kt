package com.example.weatherapp.ui.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.model.pojos.FavouriteCountry

class FavouriteCountriesAdapter(
    private val onItemClicked: OnFavItemClicked,
    private val onDeleteClicked: (FavouriteCountry) -> Unit
) : ListAdapter<FavouriteCountry, FavouriteCountriesAdapter.FavoriteCountryViewHolder>(FavouriteCountryDiffCallback()) {

    class FavoriteCountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countryName: TextView = itemView.findViewById(R.id.favCountryName)
        val temperature: TextView = itemView.findViewById(R.id.favCountryMinMaxTemp)
        val weatherIcon: ImageView = itemView.findViewById(R.id.favCountryWeatherStatusIcon)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteCountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fav_item, parent, false)
        return FavoriteCountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteCountryViewHolder, position: Int) {
        val country = getItem(position)
        holder.countryName.text = country.name
        holder.temperature.text = "${(country.minTemp + country.maxTemp) / 2}°C"

        // Load weather icon using Glide
        country.weatherIcon?.let { icon ->
            if (icon.isNotEmpty()) {
                val resourceName = "ic_$icon"
                val resourceId = holder.itemView.context.resources.getIdentifier(
                    resourceName,
                    "drawable",
                    holder.itemView.context.packageName
                )
                if (resourceId != 0) {
                    Glide.with(holder.itemView.context)
                        .load(resourceId)
                        .into(holder.weatherIcon)
                } else {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.ic_launcher_background)
                        .into(holder.weatherIcon)
                }
            } else {
                holder.weatherIcon.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        // Set click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClicked.onFavItemClicked(country.longitude, country.latitude)
        }

        // Set click listener on the delete button with confirmation dialog
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Favorite")
                .setMessage("Are you sure you want to delete ${country.name}?")
                .setPositiveButton("Yes") { _, _ ->
                    onDeleteClicked(country)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }

    class FavouriteCountryDiffCallback : DiffUtil.ItemCallback<FavouriteCountry>() {
        override fun areItemsTheSame(oldItem: FavouriteCountry, newItem: FavouriteCountry): Boolean {
            return oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(oldItem: FavouriteCountry, newItem: FavouriteCountry): Boolean {
            return oldItem == newItem
        }
    }
}