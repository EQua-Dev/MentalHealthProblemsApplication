package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object Common {

    val IRISH_CODE = "+353"
    var facilityName = "Facility"
    var clientName = "Client"

    var serviceToRate = ""

    //lateinit var mAuth: FirebaseAuth// = FirebaseAuth.getInstance()
    val auth = FirebaseAuth.getInstance()
    val clientCollectionRef = Firebase.firestore.collection("Clients")
    val facilityCollectionRef = Firebase.firestore.collection("Facilities")
    val appointmentsCollectionRef = Firebase.firestore.collection("Appointments")
    val servicesCollectionRef = Firebase.firestore.collection("Services")
    val feedbackCollectionRef = Firebase.firestore.collection("Feedbacks")

    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)
    }

    fun getDateFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()
    }



}