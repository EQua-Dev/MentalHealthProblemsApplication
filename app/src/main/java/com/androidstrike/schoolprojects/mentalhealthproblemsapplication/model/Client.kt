package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model

data class Client(
    val userId: String = "",
    val userFirstName: String = "",
    val userLastName: String = "",
    val userEmail: String = "",
    val userAddress: String = "",
    //val userAddressLongitude: String = "",
    //val userAddressLatitude: String = "",
    val dateJoined: String = "",
    val role: String = "client",
    val userPhoneNumber: String = "",
    val wallet: String = ""

)
