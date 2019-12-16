package hu.ait.foodfinder.data

import java.io.Serializable

data class Food(
    var uid: String = "",
    var name: String = "",
    var description: String = "",
    var location: String = "",
    var xCoord: Double = 44.460876,
    var yCoord: Double = -93.153638,
    var imageUrl: String = "",
    var expiryTime: String = ""
) : Serializable