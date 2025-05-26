package com.example.weatherapp.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentFavouritesBinding
import com.example.weatherapp.model.repository.WeatherAppRepository
import com.google.android.material.snackbar.Snackbar

class FavouritesFragment : Fragment(), OnFavItemClicked {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavouritesViewModel by viewModels {
        FavouritesViewModelFactory(getWeatherRepository())
    }

    private val sharedLocationViewModel: SharedLocationViewModel by viewModels({ requireActivity() })

    private lateinit var adapter: FavouriteCountriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavouriteCountriesAdapter(this) { country ->
            viewModel.deleteFavoriteCountry(country.name, country.latitude, country.longitude)
            Snackbar.make(binding.root, "Deleted ${country.name}", Snackbar.LENGTH_SHORT).show()
        }
        binding.favRV.layoutManager = LinearLayoutManager(requireContext())
        binding.favRV.adapter = adapter

        viewModel.favoriteCountries.observe(viewLifecycleOwner, Observer { countries ->
            adapter.submitList(countries)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        })

        // Handle FAB click
        val fab = (requireActivity() as MainActivity).getFab()
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_nav_favourites_to_mapFragment)
        }

        // Listen for map fragment result
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            val lat = bundle.getDouble("lat")
            val lon = bundle.getDouble("lon")
            viewModel.addFavoriteCountry(lat.toFloat(), lon.toFloat())
            Snackbar.make(binding.root, "Added favorite location: Lat $lat, Lon $lon", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Ensure FAB is visible
        (requireActivity() as MainActivity).getFab().visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        // Hide FAB when leaving fragment
        (requireActivity() as MainActivity).getFab().visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getWeatherRepository(): WeatherAppRepository {
        return (requireActivity() as MainActivity).weatherRepository
    }

    override fun onFavItemClicked(long: Float, lat: Float) {
        sharedLocationViewModel.setSelectedCoordinates(lat, long)
        findNavController().navigate(R.id.action_nav_favourites_to_nav_home)
    }
}