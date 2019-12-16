package hu.ait.foodfinder

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import hu.ait.foodfinder.data.Food
import kotlinx.android.synthetic.main.activity_information.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InformationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var xCoord : Double = 0.0
    private var yCoord : Double = 0.0
    private lateinit var food : Food

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        food = intent.getSerializableExtra(getString(R.string.key_food)) as Food

        var formatter = DateTimeFormatter.ofPattern(getString(R.string.time_pattern))
        var datetime = LocalDateTime.parse(food.expiryTime)
        var formattedDateTime = datetime.format(formatter)

        tvName.text = getString(R.string.food_name) + food.name
        tvDescription.text = getString(R.string.food_description) + food.description
        tvLocation.text = getString(R.string.food_location) + food.location
        tvTimes.text = getString(R.string.food_time) + formattedDateTime

        xCoord = food.xCoord
        yCoord = food.yCoord

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (food.imageUrl.isEmpty()) {
            ivPhoto.visibility = View.GONE
            tvImageDescription.visibility = View.GONE
        } else {
            ivPhoto.visibility = View.VISIBLE
            tvImageDescription.visibility = View.VISIBLE
            Glide.with(this@InformationActivity).load(food.imageUrl).into(ivPhoto)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        var mMap = googleMap
        val location = LatLng(xCoord, yCoord)
        mMap.addMarker(MarkerOptions().position(location).title(food.name))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(17.5f)
            .build()

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}
