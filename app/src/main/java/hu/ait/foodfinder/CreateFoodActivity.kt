package hu.ait.foodfinder

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sucho.placepicker.AddressData
import com.sucho.placepicker.Constants
import com.sucho.placepicker.MapType
import com.sucho.placepicker.PlacePicker
import hu.ait.foodfinder.data.Food
import kotlinx.android.synthetic.main.activity_create_food.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

class CreateFoodActivity : AppCompatActivity(), MyLocationProvider.OnNewLocationAvailable {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
        private const val CAMERA_REQUEST_CODE = 102
    }

    private var uploadBitmap : Bitmap? = null
    private var addressData : AddressData? = null
    private lateinit var currentLocation : Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_food)

        btnSend.setOnClickListener {
            if (uploadBitmap != null) {
                try {
                    uploadPostWithImage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                uploadFood()
            }
        }

        btnAttach.setOnClickListener {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE)
        }

        btnLocation.setOnClickListener {
            showPlacePicker()
        }
        requestNeededPermission()


    }

    private fun showPlacePicker() {
        val intent = PlacePicker.IntentBuilder()
            .setLatLong(44.460876, -93.153638)  // Initial Latitude and Longitude the Map will load into
            .showLatLong(true)  // Show Coordinates in the Activity
            .setMapZoom(18.0f)
            .setMarkerImageImageColor(R.color.colorPrimary)
            .setFabColor(R.color.colorPrimary)
            .setPrimaryTextColor(R.color.textBlack) // Change text color of Shortened Address
            .setSecondaryTextColor(R.color.textGrey) // Change text color of full Address
            .setMapType(MapType.NORMAL)
            .onlyCoordinates(true)  //Get only Coordinates from Place Picker
            .build(this)
        startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uploadBitmap = data!!.extras!!.get("data") as Bitmap
            imgAttach.setImageBitmap(uploadBitmap)
            imgAttach.visibility = View.VISIBLE
        } else if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                addressData = data?.getParcelableExtra(Constants.ADDRESS_INTENT)
            }
        }
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                Toast.makeText(this,
                    getString(R.string.request_camera), Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE)
        } else {
            // we already have permission
            btnAttach.visibility = View.VISIBLE
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this,
                    getString(R.string.location_request), Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE)
        } else {
            btnLocation.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.not_granted), Toast.LENGTH_SHORT).show()
                    btnAttach.visibility = View.GONE
                    btnLocation.visibility = View.GONE
                }
            }
        }
    }

    private fun uploadFood(imageUrl: String = "") {
        val timeString = spTimes.selectedItem.toString()
        val currentTime = LocalDateTime.now()
        lateinit var expiryTime : LocalDateTime

        when (timeString) {
            getString(R.string.one_min) -> expiryTime = currentTime.plusMinutes(1)
            getString(R.string.ten_min) -> expiryTime = currentTime.plusMinutes(10)
            getString(R.string.thirty_min) -> expiryTime = currentTime.plusMinutes(30)
            getString(R.string.one_hour) -> expiryTime = currentTime.plusHours(1)
            getString(R.string.two_hours) -> expiryTime = currentTime.plusHours(2)
            getString(R.string.three_hours) -> expiryTime = currentTime.plusHours(3)
            getString(R.string.six_hours) -> expiryTime = currentTime.plusHours(6)
        }
        var longitude = -93.153638
        var latitude = 44.460876
        if (addressData != null) {
            latitude = addressData!!.latitude
            longitude = addressData!!.longitude
        }

        val food = Food(
            FirebaseAuth.getInstance().currentUser!!.uid,
            etName.text.toString(),
            etDescription.text.toString(),
            etLocation.text.toString(),
            latitude,
            longitude,
            imageUrl,
            expiryTime.toString()
        )

        val foodCollection = FirebaseFirestore.getInstance().collection(getString(R.string.resource_path))
        foodCollection.add(food).addOnSuccessListener {
            Toast.makeText(this@CreateFoodActivity, "Food uploaded", Toast.LENGTH_LONG).show()

            finish()

        }.addOnFailureListener {
            Toast.makeText(this@CreateFoodActivity, it.message, Toast.LENGTH_LONG).show()
        }
    }

    @Throws(Exception::class)
    private fun uploadPostWithImage() {

        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener {
                Toast.makeText(this@CreateFoodActivity, it.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                newImagesRef.downloadUrl.addOnCompleteListener {
                        task -> uploadFood(task.result.toString())
                }
            }
    }

    override fun onNewLocation(location: Location) {
        currentLocation = location
    }

    private lateinit var myLocationProvider: MyLocationProvider

    private fun startLocation() {
        myLocationProvider = MyLocationProvider(this,
            this)
        myLocationProvider.startLocationMonitoring()
    }

    private fun stopLocation() {
        myLocationProvider.stopLocationMonitoring()
    }
}
