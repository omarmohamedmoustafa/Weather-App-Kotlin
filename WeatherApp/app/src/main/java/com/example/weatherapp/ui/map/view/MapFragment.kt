package com.example.weatherapp.ui.map.view

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.databinding.FragmentMapViewBinding
import com.example.weatherapp.ui.map.view_model.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException

class MapFragment : Fragment() {
    private var _binding: FragmentMapViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: MapView
    private lateinit var marker: Marker
    private lateinit var geocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Geocoder
        geocoder = Geocoder(requireContext())

        // Configure osmdroid
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = "WeatherApp/1.0"

        // Initialize map
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        // Default: London
        val startPoint = GeoPoint(51.5074, -0.1278)
        mapController.setZoom(5.0)
        mapController.setCenter(startPoint)

        marker = Marker(map)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.isDraggable = true
        map.overlays.add(marker)
        viewModel.setSelectedLocation(startPoint.latitude, startPoint.longitude)

        // Handle map tap to move marker
        map.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val proj = map.projection
                val geoPoint = proj.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                marker.position = geoPoint
                viewModel.setSelectedLocation(geoPoint.latitude, geoPoint.longitude)
                map.invalidate()
                true
            } else {
                false
            }
        }

        // Handle search
        binding.searchButton.setOnClickListener {
            performSearch()
        }

        // Handle search on keyboard action
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.selectedLocation.value?.let { (lat, lon) ->
                setFragmentResult("locationRequestKey", Bundle().apply {
                    putDouble("lat", lat)
                    putDouble("lon", lon)
                })
                findNavController().navigateUp()
            } ?: Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch() {
        val query = binding.searchInput.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a place name", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val geoPoint = GeoPoint(address.latitude, address.longitude)

                // Update marker position
                marker.position = geoPoint
                viewModel.setSelectedLocation(geoPoint.latitude, geoPoint.longitude)

                // Center map on new location
                map.controller.setCenter(geoPoint)
                map.controller.setZoom(12.0)
                map.invalidate()
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error searching location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}