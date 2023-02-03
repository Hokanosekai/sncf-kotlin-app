package fr.hoka.sncf

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import fr.hoka.sncf.databinding.ActivityMapsBinding
import fr.hoka.sncf.entities.Station
import fr.hoka.sncf.entities.Train

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnPolylineClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setOnPolylineClickListener(this)
        googleMap.setOnPolygonClickListener(this)

        // Get the train from the intent
        val train = intent.extras?.get("train") as Train

        // Create a list of LatLng for the stops
        val stops: List<LatLng> = train.stops.map { LatLng(it.getStation().lat, it.getStation().long) }

        // Calculate the average of the stops
        val latAverage = train.stops.map { it.getStation().lat }.average()
        val longAverage = train.stops.map { it.getStation().long }.average()

        // Add a polyline to the map
        googleMap.addPolyline(PolylineOptions()
            .clickable(true)
            .addAll(stops)
        )

        // Add a marker for each stop
        train.stops.forEach { addMarker(it.getStation()) }

        // Move the camera to the average of the stops
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latAverage, longAverage), 8f))
    }

    override fun onPolygonClick(p0: Polygon?) {
        println("Oe")
    }

    override fun onPolylineClick(p0: Polyline?) {
        println("Oe")
    }

    /**
     * Adds a marker for each stop
     * @param station The station to add a marker for
     */
    private fun addMarker(station: Station) {
        val marker = LatLng(station.lat, station.long)
        mMap.addMarker(MarkerOptions().position(marker).title(station.lib))
    }
}