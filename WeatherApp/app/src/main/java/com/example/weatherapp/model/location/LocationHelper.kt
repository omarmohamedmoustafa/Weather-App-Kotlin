//package com.example.weatherapp.model.location
//
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.location.Geocoder
//import android.os.Looper
//import androidx.annotation.RequiresPermission
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//
//class LocationHelper(
//    private val context: Context,
//    var onLocationUpdate: (Double?, Double?, String?) -> Unit
//) {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//
//    init {
//        setupLocationClient()
//    }
//
//    fun hasLocationPermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    fun startLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(0).apply {
//            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
//        }.build()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                val longitude = locationResult.lastLocation?.longitude
//                val latitude = locationResult.lastLocation?.latitude
//                val address = if (longitude != null && latitude != null) {
//                    getAddressFromLocation(latitude, longitude)
//                } else {
//                    null
//                }
//                onLocationUpdate(longitude, latitude, address)
//            }
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
//    }
//
//    fun stopLocationUpdates() {
//        if (::locationCallback.isInitialized) {
//            fusedLocationClient.removeLocationUpdates(locationCallback)
//        }
//    }
//
//    private fun setupLocationClient() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//    }
//
//    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
//        return try {
//            val geocoder = Geocoder(context)
//            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
//            if (!addressList.isNullOrEmpty()) {
//                addressList[0].getAddressLine(0)
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//}

package com.example.weatherapp.model.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationHelper(
    private val context: Context,
    var onLocationUpdate: (Double?, Double?, String?) -> Unit
) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    init {
        setupLocationClient()
    }

    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(0).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val longitude = locationResult.lastLocation?.longitude
                val latitude = locationResult.lastLocation?.latitude
                val address = if (longitude != null && latitude != null) {
                    getAddressFromLocation(latitude, longitude)
                } else {
                    null
                }
                onLocationUpdate(longitude, latitude, address)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context)
        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        return if (!addressList.isNullOrEmpty()) {
            addressList[0].getAddressLine(0)
        } else {
            null
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getLastKnownLocation(): Triple<Double?, Double?, String?> = suspendCoroutine { continuation ->
        if (!hasLocationPermissions()) {
            continuation.resumeWithException(Exception("Location permissions not granted"))
            return@suspendCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                val latitude = location?.latitude
                val longitude = location?.longitude
                val address = if (latitude != null && longitude != null) {
                    getAddressFromLocation(latitude, longitude)
                } else {
                    null
                }
                continuation.resume(Triple(latitude, longitude, address))
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}