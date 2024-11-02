package com.example.waywego.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.waywego.databinding.FragmentHomeBinding
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.geojson.Point

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isCameraCentered = false // Flag to center only once
    private var lastKnownLocation: Point? = null // Variable to store the last known location

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (isLocationPermissionGranted()) {
            setupMapView()
        } else {
            requestLocationPermission()
        }

        return root
    }

    private fun setupMapView() {
        with(binding.mapView) {
            location.locationPuck = createDefault2DPuck(withBearing = true)
            location.enabled = true
            location.puckBearing = PuckBearing.COURSE
            location.puckBearingEnabled = true

            // Center the camera on the user's location only once initially
            location.addOnIndicatorPositionChangedListener { point ->
                lastKnownLocation = point // Update the last known location
                if (!isCameraCentered) {
                    binding.mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )
                    isCameraCentered = true // Prevent further recentering
                }
            }

            // Re-center button to move the camera to the last known location
            binding.recenterButton.setOnClickListener {
                lastKnownLocation?.let { locationPoint ->
                    binding.mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(locationPoint)
                            .zoom(14.0)
                            .build()
                    )
                }
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            setupMapView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
