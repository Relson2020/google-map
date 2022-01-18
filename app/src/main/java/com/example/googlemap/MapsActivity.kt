package com.example.googlemap

import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.googlemap.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.PolylineOptions
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val permissionId = 1
    private lateinit var fusedLocation: FusedLocationProviderClient


    private var currentLocationLatitude: Double = 26.9124
    private var currentLocationLongitude: Double = 75.7873

    private val locationRequest = LocationRequest.create().apply {
        interval = 4000
        fastestInterval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(request: LocationResult) {
            super.onLocationResult(request)
            for (location in request.locations) {
                currentLocationLatitude = location.latitude
                currentLocationLongitude = location.longitude
                val currentLocation = LatLng(currentLocationLatitude, currentLocationLongitude)
                mMap.addMarker(MarkerOptions().position(currentLocation).title("current Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
                mMap.maxZoomLevel
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //userLocation

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        binding.locationButton.setOnClickListener {
            if ((ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
               locationSetting()
            } else {
                getPermission()
            }
        }
    }

    private fun locationSetting() {
        val locationSettingRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val locationClient = LocationServices.getSettingsClient(this)
        val task = locationClient.checkLocationSettings(locationSettingRequest.build())
        task.addOnSuccessListener {
            startLocationRequest()
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(this, 101)
                } catch (e: IntentSender.SendIntentException) {

                }
            }
        }
    }

    private fun startLocationRequest() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocation.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationRequest() {
        fusedLocation.removeLocationUpdates(locationCallback)
    }

    private fun getPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission")
                .setMessage("Accept the permission for accessing the location")
                .setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        permissionId
                    )
                }
                .setNegativeButton("Cancel") { _, _ ->

                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                permissionId
            )
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        // Add a marker in Sydney and move the camera
//        val userCurrentLocation = LatLng(currentLocationLatitude, currentLocationLongitude)
//        mMap.addMarker(MarkerOptions().position(userCurrentLocation).title("Jaipur"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(userCurrentLocation))
//        //QutabMinar
//        val qutabMinar = LatLng(28.5245, 77.1855)
//        mMap.addMarker(MarkerOptions().position(qutabMinar).title("Qutab Minar"))
//
//        val jaipur = LatLng(26.9124, 75.7873)
//        mMap.addMarker(MarkerOptions().position(jaipur).title("Jaipur"))
//
//        val polyline = mMap.addPolyline(PolylineOptions()
//            .clickable(true)
//            .color(333)
//            .width()
//            .add(tajMahal,jaipur,qutabMinar,tajMahal)
//        )
//        polyline.tag = "A"
//        polyline.color =
    }

    override fun onStop() {
        super.onStop()
        stopLocationRequest()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                locationSetting()
            }
        }
    }
}